import requests
import random
from datetime import datetime, timedelta

BASE_URL = "http://localhost:8099/talk2me/api/v1"

def create_user_with_profile(username, password="test123"):
    requests.post(f"{BASE_URL}/auth/register", json={
        "username": username,
        "password": password
    })

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
    resp = requests.put(f"http://localhost:8099/talk2me/api/users/profile", json=profile, headers=headers)
    print(f"Created user {username} with profile: {resp.status_code}")

    return token

def create_posts(token, username, count=2):
    headers = {"Authorization": f"Bearer {token}"}
    titles = [
        "Spring Boot最佳实践分享",
        "Java 21新特性体验",
        "微服务架构设计思考",
        "数据库优化技巧",
        "前端框架选型讨论",
        "Docker容器化实践",
        "API设计规范",
        "代码重构经验",
        "性能优化案例",
        "技术栈选择建议"
    ]

    for i in range(count):
        title = random.choice(titles)
        data = {
            "sectionId": random.randint(1, 6),
            "title": title,
            "content": f"这是关于{title}的详细内容，包含了实践经验和技术分享。"
        }
        resp = requests.post(f"{BASE_URL}/posts", json=data, headers=headers)
        if resp.status_code == 200:
            print(f"  Created post by {username}: {title}")

if __name__ == "__main__":
    for i in range(10):
        username = f"user{i+1}"
        token = create_user_with_profile(username)
        if token:
            create_posts(token, username)
