velocity 1.7兼容包，使用方法：
全局排除velocity2.5包

api.exclude group: 'com.bbossgroups', module: 'bboss-velocity'

导入兼容包：

api ( [group: 'com.bbossgroups.plugins', name: 'bboss-elasticsearch-rest-bc', version: "${es_version}", transitive: true])