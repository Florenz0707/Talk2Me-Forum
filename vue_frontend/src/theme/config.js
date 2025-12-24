// 主题配置文件
// 定义主题颜色变量
export const themeConfig = {
  // 主题强调色
  primary: '#667eea', // 强调色一
  secondary: '#764ba2', // 强调色二
  tertiary: '#f093fb', // 强调色三
  quaternary: '#4facfe', // 强调色四

  // 中性色
  dark: '#2d3748',
  gray: '#718096',
  lightGray: '#e2e8f0',
  background: '#f7fafc',

  // 功能色
  success: '#48bb78',
  warning: '#ed8936',
  danger: '#f56565',
  info: '#4299e1'
}

// 导出CSS变量格式的主题
export const cssVariables = {
  '--primary-color': themeConfig.primary,
  '--secondary-color': themeConfig.secondary,
  '--tertiary-color': themeConfig.tertiary,
  '--quaternary-color': themeConfig.quaternary,
  '--dark-color': themeConfig.dark,
  '--gray-color': themeConfig.gray,
  '--light-gray-color': themeConfig.lightGray,
  '--background-color': themeConfig.background,
  '--success-color': themeConfig.success,
  '--warning-color': themeConfig.warning,
  '--danger-color': themeConfig.danger,
  '--info-color': themeConfig.info
}
