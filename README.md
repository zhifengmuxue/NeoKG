# NeoKG - 智能知识图谱管理系统

NeoKG 是一个基于 Spring Boot 和 Vue 3 构建的全栈知识图谱管理系统。它集成了图数据库 Neo4j、向量检索与大模型能力（Spring AI Alibaba + Qwen），旨在提供从文档解析、知识抽取、图谱构建到智能问答（RAG）的一站式解决方案。

## ✨ 核心功能

### 1. 知识图谱构建与管理
- **文档解析**：支持多种格式文档上传（基于 Apache Tika），自动提取文本内容。
- **智能抽取**：利用大模型（Qwen）自动从非结构化文本中抽取实体（Entity）和关系（Relation）。
- **元数据管理**：自定义实体类型和关系类型的元数据定义。
- **图谱存储**：基于 Neo4j 图数据库存储复杂的实体关系网络。

### 2. 图谱分析与可视化
- **交互式可视化**：前端集成 Cytoscape.js，提供直观的知识图谱展示与交互。
- **图算法支持**：
  - **社区发现**：基于 Louvain 算法的社区检测。
  - **关键节点识别**：基于 PageRank 算法识别图谱中的核心实体。
  - **路径查找**：支持实体间的最短路径分析（BFS）。
  - **降维分析**：提供高维数据的降维可视化分析。

### 3. 智能问答 (RAG)
- **AI 对话助手**：集成 Spring AI Alibaba，支持流式对话（Server-Sent Events）。
- **检索增强生成**：结合向量数据库与知识图谱上下文，提供基于私有知识库的精准问答。
- **MCP 支持**：实现了 Model Context Protocol (MCP) 端点，允许外部 AI 代理直接调用图谱分析工具（如查询图谱、计算指标等）。

### 4. 系统管理
- **用户认证**：基于 Sa-Token 的用户登录、注册与权限管理。
- **数据监控**：图谱指标监控与异常检测。

## 🛠 技术栈

### 后端 (NeoKG-backend)
- **核心框架**: Spring Boot 3.5.5, Java 21
- **AI & LLM**: Spring AI Alibaba (Qwen/DashScope), Solon AI MCP
- **数据库**: 
  - Neo4j (图数据)
  - PostgreSQL (元数据 & 关系数据)
  - Redis (缓存 & 会话)
- **ORM**: MyBatis Plus, Spring Data Neo4j
- **工具库**: Apache Tika (文档解析), Smile (机器学习), Hutool, Lombok

### 前端 (NeoKG-front)
- **框架**: Vue 3, TypeScript, Vite
- **UI 组件库**: Ant Design Vue
- **可视化**: Cytoscape.js (图谱), ECharts (图表)
- **状态管理**: Pinia
- **网络请求**: Axios

### 部署与运维
- **容器化**: Docker, Docker Compose

## 🚀 快速开始

### 前置要求
- JDK 21+
- Node.js 20+
- Docker & Docker Compose
- Neo4j 数据库
- PostgreSQL 数据库
- Redis

### 环境配置

在 `NeoKG-backend/src/main/resources/application.yml` 或环境变量中配置以下关键信息：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${BAILIAN_KEY} # 阿里云百炼 API Key
  datasource:
    url: jdbc:postgresql://localhost:5432/NeoKG
    username: NeoKG
    password: NeoKG
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: neo4j123
  data:
    redis:
      host: localhost
```

### Docker 一键部署

项目提供了 Docker Compose 配置，可快速启动全套服务。

```bash
cd docker
# 构建镜像
docker compose build --no-cache web
# 启动服务
docker compose up -d
```

### 本地开发运行

1. **启动后端**
   ```bash
   cd NeoKG-backend
   ./mvnw spring-boot:run
   ```

2. **启动前端**
   ```bash
   cd NeoKG-front
   npm install
   npm run dev
   ```

## 📂 项目结构

```
NeoKG/
├── NeoKG-backend/       # Spring Boot 后端源码
│   ├── src/main/java/   # Java 源代码
│   └── src/main/resources/ # 配置文件与资源
├── NeoKG-front/         # Vue 3 前端源码
│   ├── src/views/       # 页面视图 (Dashboard, Graph, Chat 等)
│   └── src/components/  # 公共组件
├── docker/              # Docker 部署配置
└── neo4j-import/        # Neo4j 初始化脚本
```

## 🤝 贡献
欢迎提交 Pull Request 或 Issue 来改进本项目。

## 📄 许可证
[MIT License](LICENSE)
