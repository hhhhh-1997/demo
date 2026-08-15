# 北京市积分落户公示名单 · 数据分析

对北京市每年公示的积分落户名单(6000+ 条)进行多维度数据分析的单页应用:快速查询筛选、年龄/单位/积分分布可视化,以及基于大模型的「AI 智能问数」。

## 项目简介

本项目是一个**纯前端静态应用**,不涉及后端服务:在浏览器内解析官方 Excel 名单,数据持久化到 `localStorage`,无需后端即可完成全部分析与 AI 问数。

六大功能板块:

| 板块 | 说明 |
|---|---|
| 总览 | KPI 指标 + 搜索筛选 + 分页表格 + 增删改 + Excel 导入 |
| 年龄分析 | 年龄分布直方图、主要年龄段、汇总统计 |
| 单位分析 | 单位入围人数 Top N、单位规模分布、按单位查询明细 |
| 积分分布 | 积分直方图、分数段人数表、汇总统计 |
| AI 智能问数 | 自然语言提问(统计/排名/明细),大模型基于数据集流式回答 |
| 大模型管理 | 大模型配置 CRUD(名称 / Base URL / API Key / 模型名),标记默认配置 |

> 数据初始为空,需通过「总览 → 导入 Excel」初始化。示例数据见 `docs/北京市2026年积分落户公示名单.xlsx`。

## 技术栈

- **前端**:Vue 3 + TypeScript + Vite,自定义 CSS(JoinBright 设计系统,支持明暗主题),ECharts(图表),SheetJS/`xlsx`(Excel 解析)
- **AI 调用**:浏览器内直连 OpenAI 兼容接口,支持流式输出与 function calling

## 目录结构

```
frontend/
├── public/            # 静态资源(logo 等)
└── src/
    ├── components/    # 六个板块组件
    ├── ai/            # AI 调用封装、工具集、prompt、历史
    ├── data/          # Excel 解析导入
    ├── stores/        # 数据 / 大模型配置状态(localStorage 持久化)
    ├── composables/   # 主题切换、toast 等
    ├── utils/         # 统计聚合纯函数
    └── styles/        # 设计令牌 + 组件样式
```

## 首次部署

### 环境要求

| 依赖 | 版本 |
|---|---|
| Node.js | ≥ 18(推荐 20+) |
| npm | 随 Node 安装 |

### 1. 获取代码

```bash
git clone <仓库地址>
cd <仓库目录>/frontend
```

### 2. 安装依赖

```bash
npm install
```

### 3. 启动开发服务器

```bash
npm run dev
```

浏览器访问 `http://localhost:5173`。

### 4. 初始化数据

进入「总览」板块,点击「导入 Excel」选择 `docs/北京市2026年积分落户公示名单.xlsx`(或你自己的名单文件)完成初始化。之后再次导入会按公示编号增量更新。

## 项目启动

```bash
cd frontend
npm run dev        # 开发模式(http://localhost:5173)
npm run build      # 生产构建(产物在 frontend/dist,可部署到任意静态服务器)
npm run preview    # 本地预览构建产物
npm run typecheck  # 类型检查
npm test           # 运行单元测试(Vitest)
```

## 注意事项

- 大模型 **API Key 以明文存储于浏览器 `localStorage`**,属已知局限,请勿在生产环境配置敏感密钥。
- 数据同样存于 `localStorage`,清理浏览器数据会导致已导入名单丢失。
- 生产构建产物为纯静态文件,可部署到任意静态服务器(如 Nginx、GitHub Pages 等)。
