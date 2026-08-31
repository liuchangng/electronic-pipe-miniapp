<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="themeOverrides">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { zhCN, dateZhCN } from 'naive-ui'

// 智能极简风主题 - 与小程序端设计令牌统一
const themeOverrides = {
  common: {
    primaryColor: '#007AFF',
    primaryColorHover: '#0066D6',
    primaryColorPressed: '#0052AC',
    primaryColorSuppl: '#3395FF',
    infoColor: '#007AFF',
    infoColorHover: '#0066D6',
    infoColorPressed: '#0052AC',
    successColor: '#34C759',
    successColorHover: '#28A745',
    successColorPressed: '#1F8C37',
    warningColor: '#FF9500',
    warningColorHover: '#E88600',
    warningColorPressed: '#CC7700',
    errorColor: '#FF3B30',
    errorColorHover: '#E0321D',
    errorColorPressed: '#C42A18',
    borderRadius: '12px',
    borderRadiusSmall: '8px',
    fontFamily: '-apple-system, BlinkMacSystemFont, "SF Pro Text", "PingFang SC", "Helvetica Neue", sans-serif',
    fontSize: '14px',
    fontSizeMedium: '14px',
    fontSizeLarge: '15px',
    bodyColor: '#F2F2F7',
    cardColor: '#FFFFFF',
    modalColor: '#FFFFFF',
    popoverColor: '#FFFFFF',
    tableColor: '#FFFFFF',
    inputColor: '#FFFFFF',
    actionColor: '#F2F2F7',
    hoverColor: 'rgba(0, 122, 255, 0.06)',
    dividerColor: '#E5E5EA',
  },
  Card: {
    borderRadius: '16px',
    boxShadow: '0 2px 12px rgba(0, 0, 0, 0.06)',
    paddingMedium: '20px',
    titleFontSizeMedium: '16px',
    titleFontWeight: '600',
  },
  Button: {
    borderRadiusMedium: '10px',
    borderRadiusSmall: '8px',
    borderRadiusLarge: '12px',
    fontWeight: '500',
    heightMedium: '36px',
    heightLarge: '40px',
  },
  DataTable: {
    thColor: '#F2F2F7',
    thColorHover: '#E8E8ED',
    tdColor: '#FFFFFF',
    tdColorHover: 'rgba(0, 122, 255, 0.04)',
    borderColor: '#E5E5EA',
    thFontWeight: '600',
    thFontSize: '13px',
    tdFontSize: '14px',
    borderRadius: '12px',
  },
  Input: {
    borderRadius: '10px',
    borderFocus: '1px solid #007AFF',
    borderHover: '1px solid #C7C7CC',
    boxShadowFocus: '0 0 0 2px rgba(0, 122, 255, 0.15)',
    heightMedium: '36px',
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '10px',
        heightMedium: '36px',
      }
    }
  },
  Menu: {
    borderRadius: '8px',
    itemHeight: '44px',
    itemIconSize: '20px',
    itemFontSize: '14px',
    itemFontWeightActive: '600',
  },
  Modal: {
    borderRadius: '16px',
  },
  Tag: {
    borderRadius: '8px',
  },
  Pagination: {
    itemBorderRadius: '8px',
  },
  Message: {
    borderRadius: '10px',
  },
  Form: {
    labelFontSizeTopMedium: '14px',
    feedbackFontSizeMedium: '13px',
  },
  Switch: {
    borderRadius: '9999px',
  },
}
</script>

<style>
/* Admin 全局样式 */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #F2F2F7;
}

/* 统一滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #C7C7CC;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #AEAEB2;
}
</style>