<template>
	<div class="home-page">
		<!-- 页面头部 -->
		<header class="page-header">
			<div class="container">
				<h1 class="page-title">论坛首页</h1>
				<div class="user-actions">
					<router-link v-if="showAuthButtons" to="/login" class="btn btn-primary">登录</router-link>
					<router-link v-if="showAuthButtons" to="/register" class="btn btn-secondary">注册</router-link>
					<router-link v-else to="/user" class="btn btn-primary">用户中心</router-link>
				</div>
			</div>
		</header>

		<!-- 主内容区域 -->
		<main class="main-content">
			<div class="container">
				<!-- 面包屑导航 -->
				<nav class="breadcrumb">
					<span>首页</span>
				</nav>

				<!-- 版块信息 -->
				<div class="forum-info">
					<h2 class="forum-title">综合讨论区</h2>
					<p class="forum-description">欢迎来到Talk2Me论坛综合讨论区，这里是用户交流的主要场所。</p>
				</div>

				<!-- 操作栏 -->
				<div class="action-bar">
					<div class="sort-options">
						<label for="sort">排序方式：</label>
						<select v-model="sortBy" id="sort" @change="handleSortChange">
							<option value="latest">最新发布</option>
							<option value="replies">回复最多</option>
							<option value="views">浏览最多</option>
						</select>
					</div>
					<div class="filter-options">
						<label for="filter">筛选：</label>
						<select v-model="filterBy" id="filter" @change="handleFilterChange">
							<option value="all">全部</option>
							<option value="hot">热门</option>
							<option value="recommended">推荐</option>
						</select>
					</div>
				</div>

				<!-- 帖子列表 -->
				<div class="thread-list">
					<table class="thread-table">
						<thead>
						<tr>
							<th class="thread-info">主题</th>
							<th class="thread-author">作者</th>
							<th class="thread-time">发布时间</th>
							<th class="thread-replies">回复</th>
							<th class="thread-views">浏览</th>
							<th class="thread-last">最后回复</th>
						</tr>
						</thead>
						<tbody>
						<tr v-for="thread in threads" :key="thread.id" class="thread-item">
							<td class="thread-info">
								<div class="thread-title">
									<router-link to="/thread/" + thread.id :title="thread.title">{{
											thread.title
										}}
									</router-link>
									<span v-if="thread.isHot" class="thread-tag hot">热门</span>
									<span v-if="thread.isRecommended" class="thread-tag recommended">推荐</span>
								</div>
							</td>
							<td class="thread-author">
								<span class="author-name">{{ thread.author }}</span>
							</td>
							<td class="thread-time">{{ formatTime(thread.createdAt) }}</td>
							<td class="thread-replies">{{ thread.replies }}</td>
							<td class="thread-views">{{ thread.views }}</td>
							<td class="thread-last">
								<div class="last-reply">
									<span class="last-author">{{ thread.lastReplyAuthor }}</span>
									<span class="last-time">{{ formatTime(thread.lastReplyTime) }}</span>
								</div>
							</td>
						</tr>
						</tbody>
					</table>
				</div>

				<!-- 分页控件 -->
				<div class="pagination">
					<button
						class="page-btn"
						:disabled="currentPage === 1"
						@click="handlePageChange(currentPage - 1)"
					>
						上一页
					</button>

					<span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>

					<button
						class="page-btn"
						:disabled="currentPage === totalPages"
						@click="handlePageChange(currentPage + 1)"
					>
						下一页
					</button>

					<div class="page-jump">
						<span>跳转至</span>
						<input
							type="number"
							v-model.number="jumpPage"
							:min="1"
							:max="totalPages"
							@keyup.enter="handlePageJump"
						/>
						<span>页</span>
						<button class="btn btn-small" @click="handlePageJump">确定</button>
					</div>
				</div>
			</div>
		</main>

		<!-- 页脚 -->
		<footer class="page-footer">
			<div class="container">
				<p>© 2024 Talk2Me论坛 - 技术支持：Vue 3 + Spring Boot</p>
			</div>
		</footer>
	</div>
</template>

<script setup>
import {ref, onMounted, computed, inject} from 'vue'
import {useRouter} from 'vue-router'

const router = useRouter()

// 从全局注入获取登录状态
const isLoggedIn = inject('isLoggedIn')
const checkLoginStatus = inject('checkLoginStatus')

// 帖子列表数据
const threads = ref([])

// 分页相关数据
const currentPage = ref(1)
const totalPages = ref(10)
const jumpPage = ref(1)

// 排序和筛选
const sortBy = ref('latest')
const filterBy = ref('all')

// 计算是否显示登录/注册按钮
const showAuthButtons = computed(() => !isLoggedIn.value)

// 模拟帖子数据
const mockThreads = [
	{
		id: 1,
		title: 'Vue 3 Composition API 最佳实践分享',
		author: '前端小能手',
		createdAt: '2024-01-15T10:30:00',
		replies: 45,
		views: 1234,
		lastReplyAuthor: '技术爱好者',
		lastReplyTime: '2024-01-16T14:20:00',
		isHot: true,
		isRecommended: true
	},
	{
		id: 2,
		title: 'Spring Boot 3.0 新特性解析',
		author: '后端架构师',
		createdAt: '2024-01-14T16:45:00',
		replies: 32,
		views: 890,
		lastReplyAuthor: 'Java开发者',
		lastReplyTime: '2024-01-15T11:15:00',
		isHot: true,
		isRecommended: false
	},
	{
		id: 3,
		title: '前端性能优化实战指南',
		author: '性能优化专家',
		createdAt: '2024-01-13T09:20:00',
		replies: 28,
		views: 765,
		lastReplyAuthor: '前端开发者',
		lastReplyTime: '2024-01-14T15:30:00',
		isHot: false,
		isRecommended: true
	},
	{
		id: 4,
		title: '微服务架构设计模式探讨',
		author: '系统架构师',
		createdAt: '2024-01-12T14:10:00',
		replies: 56,
		views: 1456,
		lastReplyAuthor: '架构学习者',
		lastReplyTime: '2024-01-16T09:45:00',
		isHot: true,
		isRecommended: true
	},
	{
		id: 5,
		title: 'TypeScript 高级类型应用',
		author: 'TS爱好者',
		createdAt: '2024-01-11T11:30:00',
		replies: 42,
		views: 987,
		lastReplyAuthor: 'TypeScript开发者',
		lastReplyTime: '2024-01-13T16:20:00',
		isHot: false,
		isRecommended: false
	},
	{
		id: 6,
		title: '数据库性能调优技巧',
		author: 'DBA专家',
		createdAt: '2024-01-10T15:45:00',
		replies: 38,
		views: 1123,
		lastReplyAuthor: '数据库开发者',
		lastReplyTime: '2024-01-15T13:10:00',
		isHot: true,
		isRecommended: false
	},
	{
		id: 7,
		title: 'React 18 新特性深入分析',
		author: 'React开发者',
		createdAt: '2024-01-09T10:20:00',
		replies: 25,
		views: 876,
		lastReplyAuthor: '前端学习者',
		lastReplyTime: '2024-01-12T14:50:00',
		isHot: false,
		isRecommended: false
	},
	{
		id: 8,
		title: 'Docker 容器化部署实践',
		author: 'DevOps工程师',
		createdAt: '2024-01-08T16:15:00',
		replies: 40,
		views: 1345,
		lastReplyAuthor: '运维工程师',
		lastReplyTime: '2024-01-14T10:30:00',
		isHot: true,
		isRecommended: true
	},
	{
		id: 9,
		title: '前端工程化最佳实践',
		author: '工程化专家',
		createdAt: '2024-01-07T09:40:00',
		replies: 35,
		views: 1098,
		lastReplyAuthor: '前端架构师',
		lastReplyTime: '2024-01-13T15:20:00',
		isHot: false,
		isRecommended: true
	},
	{
		id: 10,
		title: 'RESTful API 设计规范',
		author: 'API设计师',
		createdAt: '2024-01-06T14:25:00',
		replies: 22,
		views: 765,
		lastReplyAuthor: '后端开发者',
		lastReplyTime: '2024-01-11T16:40:00',
		isHot: false,
		isRecommended: false
	}
]

// 格式化时间
const formatTime = (timeString) => {
	const date = new Date(timeString)
	const now = new Date()
	const diff = now - date

	// 计算时间差
	const minutes = Math.floor(diff / (1000 * 60))
	const hours = Math.floor(diff / (1000 * 60 * 60))
	const days = Math.floor(diff / (1000 * 60 * 60 * 24))

	if (minutes < 60) {
		return `${minutes}分钟前`
	} else if (hours < 24) {
		return `${hours}小时前`
	} else if (days < 30) {
		return `${days}天前`
	} else {
		return date.toLocaleDateString()
	}
}

// 加载帖子数据
const loadThreads = () => {
	// 这里可以替换为真实的API调用
	threads.value = mockThreads
}

// 处理排序变化
const handleSortChange = () => {
	console.log('排序方式变更为:', sortBy.value)
	// 这里可以添加排序逻辑
	loadThreads()
}

// 处理筛选变化
const handleFilterChange = () => {
	console.log('筛选条件变更为:', filterBy.value)
	// 这里可以添加筛选逻辑
	loadThreads()
}

// 处理分页变化
const handlePageChange = (page) => {
	if (page >= 1 && page <= totalPages.value) {
		currentPage.value = page
		jumpPage.value = page
		loadThreads()
	}
}

// 处理页面跳转
const handlePageJump = () => {
	let page = jumpPage.value
	if (page < 1) page = 1
	if (page > totalPages.value) page = totalPages.value

	currentPage.value = page
	jumpPage.value = page
	loadThreads()
}

// 页面加载时初始化数据
onMounted(() => {
	checkLoginStatus()
	loadThreads()
})
</script>

<style scoped>
.home-page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #f5f7fa;
}

/* 页面头部 */
.page-header {
	background-color: #2c3e50;
	color: white;
	padding: 20px 0;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header .container {
	display: flex;
	justify-content: space-between;
	align-items: center;
	max-width: 1200px;
	margin: 0 auto;
	padding: 0 20px;
}

.page-title {
	margin: 0;
	font-size: 28px;
	font-weight: 600;
}

.user-actions {
	display: flex;
	gap: 12px;
}

/* 主内容区域 */
.main-content {
	flex: 1;
	padding: 30px 0;
}

.main-content .container {
	max-width: 1200px;
	margin: 0 auto;
	padding: 0 20px;
}

/* 面包屑导航 */
.breadcrumb {
	margin-bottom: 15px;
	font-size: 14px;
	color: #666;
}

/* 版块信息 */
.forum-info {
	background-color: white;
	padding: 20px;
	border-radius: 8px;
	margin-bottom: 20px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.forum-title {
	margin: 0 0 10px 0;
	font-size: 24px;
	font-weight: 600;
	color: #333;
}

.forum-description {
	margin: 0;
	color: #666;
}

/* 操作栏 */
.action-bar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
	background-color: white;
	padding: 15px 20px;
	border-radius: 8px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.sort-options, .filter-options {
	display: flex;
	align-items: center;
	gap: 8px;
}

.sort-options label, .filter-options label {
	font-size: 14px;
	color: #666;
}

.sort-options select, .filter-options select {
	padding: 6px 10px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 14px;
}

/* 帖子列表 */
.thread-list {
	background-color: white;
	border-radius: 8px;
	overflow: hidden;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.thread-table {
	width: 100%;
	border-collapse: collapse;
}

.thread-table thead {
	background-color: #f8f9fa;
	border-bottom: 2px solid #e9ecef;
}

.thread-table th {
	padding: 15px 20px;
	text-align: left;
	font-weight: 600;
	color: #333;
	font-size: 14px;
}

.thread-table td {
	padding: 15px 20px;
	border-bottom: 1px solid #e9ecef;
	font-size: 14px;
}

.thread-info {
	width: 50%;
}

.thread-title {
	display: flex;
	align-items: center;
	gap: 8px;
}

.thread-title a {
	color: #2c3e50;
	text-decoration: none;
	font-weight: 500;
	line-height: 1.5;
}

.thread-title a:hover {
	color: #3498db;
}

.thread-tag {
	padding: 2px 8px;
	border-radius: 12px;
	font-size: 12px;
	font-weight: 500;
}

.thread-tag.hot {
	background-color: #ffebee;
	color: #c62828;
}

.thread-tag.recommended {
	background-color: #e3f2fd;
	color: #1565c0;
}

.thread-author {
	width: 15%;
}

.author-name {
	color: #666;
}

.thread-time, .thread-replies, .thread-views {
	width: 10%;
	color: #999;
	text-align: center;
}

.thread-last {
	width: 15%;
	color: #666;
}

.last-reply {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.last-author {
	font-weight: 500;
}

.last-time {
	font-size: 12px;
	color: #999;
}

/* 分页控件 */
.pagination {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 30px;
	padding: 20px;
	background-color: white;
	border-radius: 8px;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-btn {
	padding: 8px 16px;
	border: 1px solid #ddd;
	background-color: white;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
	color: #666;
	transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
	background-color: #f8f9fa;
	border-color: #ccc;
}

.page-btn:disabled {
	cursor: not-allowed;
	opacity: 0.5;
}

.page-info {
	font-size: 14px;
	color: #666;
}

.page-jump {
	display: flex;
	align-items: center;
	gap: 8px;
	font-size: 14px;
	color: #666;
}

.page-jump input {
	width: 60px;
	padding: 6px;
	border: 1px solid #ddd;
	border-radius: 4px;
	text-align: center;
}

.btn-small {
	padding: 4px 8px;
	font-size: 12px;
}

/* 页脚 */
.page-footer {
	background-color: #2c3e50;
	color: white;
	text-align: center;
	padding: 20px 0;
	margin-top: auto;
}

.page-footer .container {
	max-width: 1200px;
	margin: 0 auto;
	padding: 0 20px;
}

/* 按钮样式 */
.btn {
	padding: 10px 20px;
	border: none;
	border-radius: 4px;
	font-size: 14px;
	font-weight: 500;
	cursor: pointer;
	text-decoration: none;
	display: inline-block;
	transition: all 0.2s;
}

.btn-primary {
	background-color: #3498db;
	color: white;
}

.btn-primary:hover {
	background-color: #2980b9;
}

.btn-secondary {
	background-color: #95a5a6;
	color: white;
}

.btn-secondary:hover {
	background-color: #7f8c8d;
}

/* 响应式设计 */
@media (max-width: 768px) {
	.page-header .container {
		flex-direction: column;
		gap: 15px;
	}

	.action-bar {
		flex-direction: column;
		gap: 15px;
		align-items: stretch;
	}

	.sort-options, .filter-options {
		justify-content: space-between;
	}

	.thread-table {
		display: block;
		overflow-x: auto;
	}

	.thread-info {
		width: 40%;
	}

	.thread-author, .thread-last {
		width: 20%;
	}

	.thread-time, .thread-replies, .thread-views {
		width: 10%;
	}

	.pagination {
		flex-direction: column;
		gap: 15px;
	}

	.page-jump {
		width: 100%;
		justify-content: center;
	}
}

@media (max-width: 480px) {
	.main-content .container {
		padding: 0 10px;
	}

	.forum-info, .action-bar, .thread-list, .pagination {
		padding: 15px;
	}

	.thread-table th, .thread-table td {
		padding: 10px;
		font-size: 13px;
	}

	.thread-title a {
		font-size: 13px;
	}

	.thread-tag {
		font-size: 11px;
		padding: 1px 6px;
	}
}
</style>
