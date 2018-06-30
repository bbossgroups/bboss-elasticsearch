# bboss-elastic

不错的elasticsearch客户端工具包,bboss es开发套件采用类似于mybatis的方式操作elasticsearch

jdk要求： jdk 1.6+

elasticsearch版本要求：2.x,5.x,6.x,+

## 快速集成和应用 
非spring boot项目：
https://my.oschina.net/bboss/blog/1801273 

spring boot项目：
https://my.oschina.net/bboss/blog/1835601

详细配置说明参考文档：
https://my.oschina.net/bboss/blog/1556866
# elastic search配置和使用

elastic search配置和使用参考文档
 
https://my.oschina.net/bboss/blog/1556866 
# 完整的demo
https://github.com/bbossgroups/elasticsearchdemo

# bboss elastic特点
https://www.oschina.net/p/bboss-elastic

# 版本升级注意事项
v5.0.5.7及后续版本废弃@PrimaryKey注解，改用@ESId注解来标注索引_id的值

v5.0.6.0及后续版本的dsl配置变量语法变更：

$xxx模式变量将直接输出变量的原始值，不会对变量进行特殊字符转义处理，也不会对变量进行日期格式化处理

请在代码中自行对$xxx模式变量进行特殊转移字符处理和日期格式化处理

只有#[xxx]格式的变量才会对特殊字符进行自动转义处理和日期格式化处理，同时为其增加escape布尔值属性，

用来控制是否对#[xxx]模式变量进行自动化转义处理，false禁用转义处理，true启用，默认启用

## License

The BBoss Framework is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0