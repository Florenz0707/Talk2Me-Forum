/**
 * CORS 跨域访问测试脚本
 * 使用 Node.js 运行此脚本测试跨域访问配置
 *
 * 运行方式:
 * 1. 确保已安装 node-fetch: npm install node-fetch
 * 2. 运行: node test/cors-test.js
 */

// 注意：Node.js 环境下的 fetch API 需要 Node.js 18+ 或使用 node-fetch 包
// 如果使用较旧版本的 Node.js，请先安装: npm install node-fetch

const API_BASE_URL = process.env.API_BASE_URL || "http://localhost:8099";
const CONTEXT_PATH = process.env.CONTEXT_PATH || "/talk2me";
const API_URL = `${API_BASE_URL}${CONTEXT_PATH}/api/v1/auth/register`;

// 测试数据
const TEST_USERNAME = process.env.TEST_USERNAME || "testuser";
const TEST_PASSWORD = process.env.TEST_PASSWORD || "testpass123";

// 颜色输出（用于终端）
const colors = {
  reset: "\x1b[0m",
  bright: "\x1b[1m",
  green: "\x1b[32m",
  red: "\x1b[31m",
  yellow: "\x1b[33m",
  blue: "\x1b[34m",
  cyan: "\x1b[36m",
};

function log(message, color = "reset") {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function logHeader(message) {
  console.log("\n" + "=".repeat(60));
  log(message, "bright");
  console.log("=".repeat(60));
}

function logSection(message) {
  console.log("\n" + "-".repeat(60));
  log(message, "cyan");
  console.log("-".repeat(60));
}

async function testRegister() {
  logHeader("🚀 测试注册请求 (POST)");

  log(`\n📤 请求信息:`, "blue");
  log(`   URL: ${API_URL}`);
  log(`   方法: POST`);
  log(
    `   请求体: ${JSON.stringify(
      { username: TEST_USERNAME, password: TEST_PASSWORD },
      null,
      2,
    )}`,
  );

  try {
    // 检查是否支持 fetch API
    let fetch;
    if (typeof globalThis.fetch === "function") {
      fetch = globalThis.fetch;
    } else {
      // 尝试导入 node-fetch
      try {
        const nodeFetch = require("node-fetch");
        fetch = nodeFetch.default || nodeFetch;
      } catch (e) {
        log("\n❌ 错误: 未找到 fetch API。请安装 node-fetch:", "red");
        log("   npm install node-fetch", "yellow");
        log("   或者使用 Node.js 18+ 版本", "yellow");
        return;
      }
    }

    const startTime = Date.now();

    const response = await fetch(API_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        Origin: "http://localhost:8900", // 模拟跨域请求
      },
      body: JSON.stringify({
        username: TEST_USERNAME,
        password: TEST_PASSWORD,
      }),
    });

    const endTime = Date.now();
    const duration = endTime - startTime;

    logSection("📥 响应信息");
    log(
      `   状态码: ${response.status} ${response.statusText}`,
      response.ok ? "green" : "red",
    );
    log(`   响应时间: ${duration} ms`);

    // 显示 CORS 响应头
    logSection("🌐 CORS 响应头");
    const corsHeaders = [
      "access-control-allow-origin",
      "access-control-allow-methods",
      "access-control-allow-headers",
      "access-control-allow-credentials",
      "access-control-max-age",
      "access-control-expose-headers",
    ];

    let foundCorsHeaders = false;
    response.headers.forEach((value, key) => {
      const lowerKey = key.toLowerCase();
      if (corsHeaders.some((h) => lowerKey.includes(h))) {
        log(`   ${key}: ${value}`, "green");
        foundCorsHeaders = true;
      }
    });

    if (!foundCorsHeaders) {
      log("   ⚠️  未找到 CORS 响应头", "yellow");
    }

    // 显示所有响应头
    logSection("📋 所有响应头");
    response.headers.forEach((value, key) => {
      log(`   ${key}: ${value}`);
    });

    // 读取响应体
    const contentType = response.headers.get("content-type");
    let responseBody;

    if (contentType && contentType.includes("application/json")) {
      responseBody = await response.json();
    } else {
      responseBody = await response.text();
    }

    logSection("📄 响应体");
    console.log(JSON.stringify(responseBody, null, 2));

    if (response.ok) {
      log("\n✅ 请求成功！", "green");
    } else {
      log("\n❌ 请求失败", "red");
    }
  } catch (error) {
    logSection("❌ 错误信息");
    log(`   错误类型: ${error.name}`, "red");
    log(`   错误信息: ${error.message}`, "red");

    if (error.stack) {
      log("\n   堆栈跟踪:", "yellow");
      console.log(error.stack);
    }

    log("\n可能的原因:", "yellow");
    log("   1. CORS 配置未正确设置");
    log("   2. 服务器未运行");
    log("   3. URL 配置错误");
    log("   4. 网络连接问题");
  }
}

async function testPreflight() {
  logHeader("🔍 测试预检请求 (OPTIONS)");

  log(`\n📤 请求信息:`, "blue");
  log(`   URL: ${API_URL}`);
  log(`   方法: OPTIONS`);
  log(`   说明: 预检请求用于检查 CORS 是否允许实际请求`);

  try {
    let fetch;
    if (typeof globalThis.fetch === "function") {
      fetch = globalThis.fetch;
    } else {
      try {
        const nodeFetch = require("node-fetch");
        fetch = nodeFetch.default || nodeFetch;
      } catch (e) {
        log("\n❌ 错误: 未找到 fetch API", "red");
        return;
      }
    }

    const startTime = Date.now();

    const response = await fetch(API_URL, {
      method: "OPTIONS",
      headers: {
        Origin: "http://localhost:3000",
        "Access-Control-Request-Method": "POST",
        "Access-Control-Request-Headers": "content-type",
      },
    });

    const endTime = Date.now();
    const duration = endTime - startTime;

    logSection("📥 响应信息");
    log(
      `   状态码: ${response.status} ${response.statusText}`,
      response.ok ? "green" : "red",
    );
    log(`   响应时间: ${duration} ms`);

    // 显示 CORS 响应头
    logSection("🌐 CORS 响应头");
    const corsHeaders = [
      "access-control-allow-origin",
      "access-control-allow-methods",
      "access-control-allow-headers",
      "access-control-allow-credentials",
      "access-control-max-age",
    ];

    let foundCorsHeaders = false;
    response.headers.forEach((value, key) => {
      const lowerKey = key.toLowerCase();
      if (corsHeaders.some((h) => lowerKey.includes(h))) {
        log(`   ${key}: ${value}`, "green");
        foundCorsHeaders = true;
      }
    });

    if (!foundCorsHeaders) {
      log("   ⚠️  未找到 CORS 响应头", "yellow");
    }

    if (response.ok) {
      log("\n✅ 预检请求成功！", "green");
    } else {
      log("\n❌ 预检请求失败", "red");
    }
  } catch (error) {
    logSection("❌ 错误信息");
    log(`   错误类型: ${error.name}`, "red");
    log(`   错误信息: ${error.message}`, "red");
  }
}

// 主函数
async function main() {
  logHeader("🌐 CORS 跨域访问测试脚本");
  log(`\n配置信息:`, "blue");
  log(`   API URL: ${API_URL}`);
  log(`   测试用户名: ${TEST_USERNAME}`);
  log(`   测试密码: ${TEST_PASSWORD.replace(/./g, "*")}`);

  // 测试预检请求
  await testPreflight();

  // 测试注册请求
  await testRegister();

  log("\n" + "=".repeat(60));
  log("测试完成！", "bright");
  log("=".repeat(60) + "\n");
}

// 运行测试
if (require.main === module) {
  main().catch((error) => {
    log(`\n❌ 未捕获的错误: ${error.message}`, "red");
    process.exit(1);
  });
}

module.exports = { testRegister, testPreflight };
