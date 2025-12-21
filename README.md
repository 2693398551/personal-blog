<div align="center">
  <h1>🚀 Personal Blog 系统</h1>
  <p>基于 Spring Boot 2.7 & Vue 3 的全栈博客解决方案</p>
  <br />
</div>

## 📂 项目模块规划
<table width="100%">
  <tr>
    <td width="50%" valign="top">
      <h3>🖥️ 后端服务 (blog-api)</h3>
      <ul>
        <li><b>核心框架：</b>Spring Boot</li>
        <li><b>构建工具：</b>Maven</li>
        <li><b>主要任务：</b>处理业务逻辑与数据持久化</li>
      </ul>
    </td>
    <td width="50%" valign="top">
      <h3>🎨 前端界面 (blog-app)</h3>
      <ul>
        <li><b>核心框架：</b>Vue.js</li>
        <li><b>环境要求：</b>Node.js</li>
        <li><b>主要任务：</b>响应式页面交互与展示</li>
      </ul>
    </td>
  </tr>
</table>

---

## 🛠️ 快速上手指南

### 1. 后端环境配置
<blockquote style="padding: 10px; background-color: #f9f9f9; border-left: 5px solid #2196F3;">
  <b>步骤 A:</b> 修改 <code>application.yml</code> 数据库连接<br/>
  <b>步骤 B:</b> 运行 <code>mvn clean install</code> 进行项目构建<br/>
  <b>步骤 C:</b> 运行 <code>BlogApp.java</code> 启动服务
</blockquote>

### 2. 前端环境配置
<blockquote style="padding: 10px; background-color: #f9f9f9; border-left: 5px solid #4CAF50;">
  <b>步骤 A:</b> 进入目录执行 <code>npm install</code> 安装依赖<br/>
  <b>步骤 B:</b> 执行 <code>npm run dev</code> 开启开发模式
</blockquote>

---

## 💡 开发小贴士
* <b>前后端联调：</b> 后端已开启 <b>CORS 跨域支持</b>，前端可直接调用接口。
* <b>结构建议：</b> 保持 <code>blog-parent</code> 为根目录，方便统一提交 Git 仓库。