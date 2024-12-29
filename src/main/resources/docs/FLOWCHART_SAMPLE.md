
```mermaid
graph TD
    A[Start] --> B{Decision Point}
    B --> |Yes| C[Process 1]
    B --> |No| D(Process 2)
    C --> E[End]
    D --> E[End]

    %% 공통 스타일 정의
    classDef common fill:#4CAF50,stroke:#333,stroke-width:2px,color:#fff;
    
    %% 스타일 적용
    style A fill:#4CAF50,stroke:#333,stroke-width:2px,color:#fff;
    style B fill:#FF9800,stroke:#333,stroke-width:2px,color:#fff;
    style C fill:#2196F3,stroke:#333,stroke-width:2px,color:#fff;
    style D fill:#F44336,stroke:#333,stroke-width:2px,color:#fff;
    style E fill:#9C27B0,stroke:#333,stroke-width:2px,color:#fff;
