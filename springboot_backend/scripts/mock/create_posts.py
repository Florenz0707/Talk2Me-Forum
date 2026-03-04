import requests
import random

BASE_URL = "http://localhost:8099/talk2me/api/v1"

def get_token():
    username = f"user_{random.randint(1000, 9999)}"
    password = "test123"

    requests.post(f"{BASE_URL}/auth/register", json={
        "username": username,
        "password": password,
        "email": f"{username}@test.com"
    })

    resp = requests.post(f"{BASE_URL}/auth/login", json={
        "username": username,
        "password": password
    })
    return resp.json()["access_token"]

def create_posts(token, count=10):
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

    for i in range(min(count, len(titles))):
        data = {
            "sectionId": random.randint(1, 3),
            "title": titles[i],
            "content": f"这是关于{titles[i]}的详细内容，包含了实践经验和技术分享。"
        }
        resp = requests.post(f"{BASE_URL}/posts", json=data, headers=headers)
        print(f"Created post: {resp.json()['data']['title']}")

if __name__ == "__main__":
    token = get_token()
    create_posts(token)
