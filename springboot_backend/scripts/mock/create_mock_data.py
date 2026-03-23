import requests
import random
from datetime import datetime, timedelta

BASE_URL = "http://localhost:8099/talk2me/api/v1"

def create_user_with_profile(username, password="test123456"):
    register_resp = requests.post(f"{BASE_URL}/auth/register", json={
        "username": username,
        "password": password
    })
    if register_resp.status_code != 200 and "already taken" not in register_resp.text:
        print(f"Register failed for {username}: {register_resp.status_code}, {register_resp.text}")
        return None

    resp = requests.post(f"{BASE_URL}/auth/login", json={
        "username": username,
        "password": password
    })

    if resp.status_code != 200:
        print(f"Login failed for {username}: {resp.status_code}, {resp.text}")
        return None

    token = resp.json()["access_token"]

    bios = [
        "热爱编程的开发者",
        "全栈工程师，喜欢探索新技术",
        "后端开发，专注于性能优化",
        "前端爱好者，追求极致用户体验",
        "架构师，关注系统设计",
        "技术博主，分享编程经验",
        "开源贡献者",
        "代码洁癖患者",
        "终身学习者",
        "技术极客"
    ]

    genders = ["男", "女", "其他"]
    occupations = ["软件工程师", "前端开发", "后端开发", "全栈开发", "架构师", "技术经理", "学生", "自由职业者"]

    birthday = datetime.now() - timedelta(days=random.randint(7300, 14600))
    profile = {
        "bio": random.choice(bios),
        "birthday": birthday.strftime("%Y-%m-%d"),
        "gender": random.choice(genders),
        "occupation": random.choice(occupations)
    }

    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.put(f"{BASE_URL}/users/profile", json=profile, headers=headers)
    print(f"Create user {username} with profile: {resp.status_code}")
    if resp.status_code != 200:
        print(f"  Profile update response: {resp.text}")

    return token

def create_posts(token, username, count=2):
    headers = {"Authorization": f"Bearer {token}"}
    post_templates = [
        {
            "title": "从零搭建Spring Boot论坛服务的踩坑记录",
            "content": "这周把论坛后端从单体雏形推进到可用版本，重点做了认证链路、统一异常处理和分层改造。\n\n"
                       "最容易忽略的问题是：开发环境里接口能跑，不代表线上配置安全。比如 token 过期、跨域、反向代理前缀这些边界都要提前验证。\n\n"
                       "如果你也在做类似项目，建议先把日志字段和错误码策略定下来，后续排错会轻松很多。"
        },
        {
            "title": "一次慢查询优化：把接口耗时从1.8s降到180ms",
            "content": "最近排查帖子列表接口时，发现分页查询在数据量增长后明显变慢。\n\n"
                       "主要优化点有三个：1）补齐高频筛选字段索引；2）避免在热路径做不必要的联表；3）把统计类字段改成异步聚合。\n\n"
                       "优化后 P95 延迟下降明显，后续还计划做缓存预热和读写分离。"
        },
        {
            "title": "关于接口设计的一点实践：版本化和幂等",
            "content": "团队最近统一了 REST 接口规范，最大的收益是前后端联调成本下降。\n\n"
                       "我们强制要求路径带版本号（如 /api/v1），并对关键写操作引入幂等约束，避免重复提交导致脏数据。\n\n"
                       "规范看起来啰嗦，但当系统规模上来后，它就是效率和稳定性的基础设施。"
        },
        {
            "title": "Java 21 在业务项目里的实际收益",
            "content": "升级到 Java 21 后，最直观的体感是并发任务调度和代码可读性都更好了。\n\n"
                       "我们把部分 IO 密集型任务切到虚拟线程，线程管理更轻量；同时利用新语法让领域对象和校验逻辑更清晰。\n\n"
                       "建议先从边缘服务试点升级，跑一轮压测和回归再全量切换。"
        },
        {
            "title": "前后端协作建议：把“可观测性”前置",
            "content": "很多联调问题不是代码本身难，而是信息不对称。\n\n"
                       "我现在会要求每个接口在开发阶段就具备请求追踪 ID、关键参数日志和可读的错误信息，前端也同步保留失败上下文。\n\n"
                       "当排查线上问题时，这些投入会立刻回本。"
        }
    ]

    for i in range(count):
        template = random.choice(post_templates)
        data = {
            "sectionId": random.randint(1, 6),
            "title": template["title"],
            "content": template["content"]
        }
        resp = requests.post(f"{BASE_URL}/posts", json=data, headers=headers)
        if resp.status_code == 200:
            print(f"  Created post by {username}: {template['title']}")
        else:
            print(f"  Create post failed for {username}: {resp.status_code}, {resp.text}")


def generate_meaningful_usernames(count):
    prefixes = [
        "backend", "frontend", "fullstack", "devops", "data",
        "qa", "architect", "mobile", "product", "sre"
    ]
    focuses = [
        "spring", "java", "redis", "mysql", "kafka",
        "docker", "cloud", "api", "forum", "search"
    ]

    names = []
    used = set()
    idx = 0
    while len(names) < count:
        base = f"{prefixes[idx % len(prefixes)]}_{focuses[(idx // len(prefixes)) % len(focuses)]}"
        suffix = random.randint(10, 99)
        username = f"{base}{suffix}"
        if username not in used:
            used.add(username)
            names.append(username)
        idx += 1
    return names

if __name__ == "__main__":
    usernames = generate_meaningful_usernames(10)
    for username in usernames:
        token = create_user_with_profile(username)
        if token:
            create_posts(token, username)
