# how to use docker to run the application

## 前置条件

1. 安装 Docker 和 Docker Compose
2. 正常的网络连接

## 编译

```bash
# 进入到此 README 文件所在目录
cd ./docker
docker compose build --no-cache web
```

## 运行

```bash
docker compose up -d
```