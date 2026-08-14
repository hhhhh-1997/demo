<script setup lang="ts">
import logo from './assets/logo-white.png'
</script>

<template>
  <nav class="nav-topbar">
    <img class="nav-brand-logo" :src="logo" alt="JoinBright" />
    <span class="nav-topbar-title">绩效管理系统</span>
    <div class="nav-actions">
      <span class="nav-avatar" aria-label="当前用户">管</span>
    </div>
  </nav>

  <main class="page-main">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>绩效管理</el-breadcrumb-item>
      <el-breadcrumb-item>绩效月报查询</el-breadcrumb-item>
    </el-breadcrumb>
    <h1 class="page-title">绩效月报查询</h1>
    <!-- Task 7 将在此挂载 <PerformanceQuery /> -->
  </main>
</template>
