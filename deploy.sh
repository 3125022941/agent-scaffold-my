#!/bin/bash
# 发布脚手架 Jar 到阿里云 Maven 私有制品库（packages.aliyun.com）。
# 前提：
#   1) 已执行 mvn archetype:create-from-project 生成脚手架；
#   2) 已在 target/generated-sources/archetype/pom.xml 填好 distributionManagement 及真实仓库地址；
#   3) 已在 ~/.m2/settings.xml 配置 release 仓库（id=2402-release-ZIxgz2）的访问凭据。
cd "$(dirname "$0")/target/generated-sources/archetype" || exit 1
mvn clean install org.apache.maven.plugins:maven-deploy-plugin:2.8:deploy -DskipTests