#!/bin/bash
# 使用本地脚手架创建新项目。
# 前提：已生成并安装脚手架（在 target/generated-sources/archetype 目录下执行 mvn install）。
# 脚本在哪个目录执行，新项目就会创建在哪个目录。
mvn archetype:generate -X -DarchetypeCatalog=local