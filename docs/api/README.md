# Web教辅系统 API 文档

本目录保存后端接口文档，方便前端、三维实训程序和测试人员查看。

## 在线查看

启动后端后访问：

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 仓库文件

- `openapi.json`: 当前后端自动生成的 OpenAPI 3.0 接口定义。

可以将 `openapi.json` 导入 Apifox、Postman、Swagger Editor 等工具查看和调试。

## 更新方式

后端接口变更后，启动后端并重新导出：

```bash
curl -fsS http://127.0.0.1:8080/v3/api-docs | jq . > docs/api/openapi.json
```
