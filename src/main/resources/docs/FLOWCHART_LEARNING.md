```
[Rectangle Node] 직사각형
{Diamond Node} 마름모
(This is a Circle) 둥근 직사각형
([Rounded Rectangle]) 가로로 긴원
[/Parallelogram Node/] 팽행 사변형
{{Hexagon Node}} 육각형
((Circle Node)) 동그라미
```
## 순서도
```mermaid
graph TD
    A[Start] --> B{Decision Point}
    B --> |Yes| C((Process 1))
    B --> |No| D[/Process 2/]
    C --> E[End]
    D --> E[End]

    style A fill:#4CAF50,stroke:#000,stroke-width:2px,color:#fff;
    style E fill:#FF5722,stroke:#000,stroke-width:2px,color:#fff;

