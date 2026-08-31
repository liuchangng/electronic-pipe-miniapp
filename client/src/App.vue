<script setup lang="ts">
import { onLaunch, onShow, onHide, onError } from '@dcloudio/uni-app'
import { useLogin } from '@/hooks/useLogin'

const { validateToken } = useLogin()

onLaunch(() => {
  // App启动时验证token有效性，无效则清除
  validateToken()
})

// 全局错误捕获：防止微信框架层面的错误（如代理环境下 access_token missing）干扰应用运行
onError((err) => {
  const errMsg = String(err)
  // 微信SDK框架错误：access_token / login 相关，属于环境问题，静默处理
  if (errMsg.includes('access_token') || errMsg.includes('login:fail')) {
    console.warn('[App] 微信框架环境错误（可忽略）:', errMsg)
    return
  }
  // 其他错误正常输出
  console.error('[App] 全局错误:', err)
})

onShow(() => {
  // App显示
})

onHide(() => {
  // App隐藏
})
</script>

<style>
@import './styles/theme.css';

/* 全局样式 */
page {
  background-color: var(--bg-primary);
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}
</style>
