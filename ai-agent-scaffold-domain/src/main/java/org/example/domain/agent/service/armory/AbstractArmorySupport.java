package org.example.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import org.apache.catalina.core.ApplicationContext;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public abstract class AbstractArmorySupport extends AbstractMultiThreadStrategyRouter<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> {
    protected final Logger log=LoggerFactory.getLogger(AbstractArmorySupport.class);
    @Resource
    protected ApplicationContext applicationContext;
    @Override
    protected void multiThread(ArmoryCommandEntity requestParameter,DefaultArmoryFactory.DynamicContext dynamicContext){

    }
    /*通用的bean注册方法*/
    protected synchronized <T> void registerBean(String beanName,Class<T>beanClass,T beanInstance){
        DefaultArmoryFactory beanFactory = (DefaultArmoryFactory) applicationContext.getAutowireCapableBeanFactory();

        //注册
        BeanDefinitionBuilder beanDefinitionBuilder=BeanDefinitionBuilder.genericBeanDefinition(beanClass,()->beanInstance);
        BeanDefinition beanDefinition=beanDefinitionBuilder.getRawBeanDefinition();
        BeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        //如果bean已存在，先移除
        if(beanFactory.containBeanDefinition(beanName)){
            beanFactory.removeBeanDefinition(beanName);
        }
        //注册新的bean
        beanFactory.registerBeanDefinition(beanName,beanDefinition);
        log.info("成功注册bean:{}",beanName);

    }
    protected <T> T getBean(String beanName){
        return (T) applicationContext.getBean(beanName);
    }

}
