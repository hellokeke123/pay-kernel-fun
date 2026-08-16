<template>
  <div class="layout">
    <header class="mobile-bar">
      <button class="hamburger" @click="menuOpen = true" aria-label="打开菜单">☰</button>
      <div class="brand">
        <span class="brand-dot" />
        <span class="brand-text">pay-kernel-fun</span>
      </div>
    </header>

    <div v-if="menuOpen" class="mask" @click="menuOpen = false" />

    <aside class="sidebar" :class="{ 'sidebar--open': menuOpen }">
      <div class="brand brand--desktop">
        <span class="brand-dot" />
        <span class="brand-text">pay-kernel-fun</span>
      </div>
      <nav class="menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          active-class="menu-item--active"
          @click.native="menuOpen = false"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <span>API: {{ apiBase }}</span>
      </div>
    </aside>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'BasicLayout',
  data() {
    return {
      apiBase: process.env.VUE_APP_BASE_API,
      menuOpen: false,
      menuItems: [
        { path: '/goods', title: '商品管理', icon: '📦' },
        { path: '/orders', title: '订单管理', icon: '🧾' },
        { path: '/trade-lab', title: '交易测试台', icon: '🧪' }
      ]
    }
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.mobile-bar {
  display: none;
}

.mask {
  display: none;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #ffffff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  padding: 20px 12px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 8px 20px;
  font-weight: 600;
  font-size: 15px;
  color: #1f2d3d;
}

.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6a8dff, #8f6bff);
  flex-shrink: 0;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  color: #5a6474;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.15s, color 0.15s;
}

.menu-item:hover {
  background: #f4f6fb;
  color: #1f2d3d;
}

.menu-item--active {
  background: linear-gradient(135deg, rgba(106, 141, 255, 0.12), rgba(143, 107, 255, 0.12));
  color: #4f6dff;
  font-weight: 600;
}

.menu-icon {
  font-size: 16px;
}

.sidebar-footer {
  padding: 8px;
  font-size: 12px;
  color: #b0b6c2;
  border-top: 1px solid #f0f1f5;
  padding-top: 12px;
  word-break: break-all;
}

.content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding: 24px 28px;
}

@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }

  .mobile-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: #ffffff;
    border-bottom: 1px solid #ebeef5;
    position: sticky;
    top: 0;
    z-index: 20;
  }

  .brand--desktop {
    display: none;
  }

  .hamburger {
    border: none;
    background: transparent;
    font-size: 20px;
    line-height: 1;
    padding: 4px 6px;
    cursor: pointer;
    color: #1f2d3d;
  }

  .mask {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    z-index: 25;
  }

  .sidebar {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 30;
    width: 240px;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
    box-shadow: 4px 0 16px rgba(0, 0, 0, 0.08);
  }

  .sidebar--open {
    transform: translateX(0);
  }

  .content {
    padding: 16px;
  }
}
</style>
