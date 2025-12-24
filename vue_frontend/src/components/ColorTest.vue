<template>
	<div class="color-test">
		<h2>CSS变量测试</h2>
		<div class="color-boxes">
			<div class="color-box" style="background-color: var(--primary-color);">
				<span>--primary-color</span>
			</div>
			<div class="color-box" style="background-color: var(--secondary-color);">
				<span>--secondary-color</span>
			</div>
			<div class="color-box" style="background-color: var(--tertiary-color);">
				<span>--tertiary-color</span>
			</div>
			<div class="color-box" style="background-color: var(--quaternary-color);">
				<span>--quaternary-color</span>
			</div>
			<div class="color-box" style="background-color: var(--text-color); color: white;">
				<span>--text-color</span>
			</div>
			<div class="color-box" style="background-color: var(--background-color); border: 1px solid #ccc;">
				<span>--background-color</span>
			</div>
			<div class="color-box" style="background-color: var(--card-background-color); border: 1px solid #ccc;">
				<span>--card-background-color</span>
			</div>
			<div class="color-box" style="background-color: var(--sapphire-blue); color: white;">
				<span>--sapphire-blue</span>
			</div>
		</div>
	</div>
</template>

<script>
export default {
	name: 'ColorTest'
}
</script>

<style scoped>
.color-test {
	padding: 20px;
	max-width: 800px;
	margin: 0 auto;
}

h2 {
	text-align: center;
	margin-bottom: 20px;
	color: var(--text-color);
}

.color-boxes {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
	gap: 15px;
}

.color-box {
	height: 100px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 8px;
	font-weight: bold;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
