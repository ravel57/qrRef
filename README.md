URL: https://qrref.io

Web-приложение для перессылки текста на экран, через сканирование QR-кода

Front-end: https://github.com/ravel57/qrref-front

Запуск Docker:
-
``` 
sudo docker build -t qrref . && sudo docker run --name qrRef -d --restart unless-stopped -p 443:443 --env url=[YOUR_DOMAIN] qrref
```

Nginx перед приложением
-
Приложение слушает `127.0.0.1:8080`, переменная окружения `url` — домен сайта (например, `qrref.io`).
WebSocket (чат «Обмен», приём текста/файлов) работает только если nginx пробрасывает `Upgrade`:

```nginx
map $http_upgrade $connection_upgrade {
    default upgrade;
    ''      close;
}

upstream qrref {
    server 127.0.0.1:8080;
}

server {
    listen 443 ssl;
    server_name qrref.io;
    # ssl_certificate ...; ssl_certificate_key ...;

    client_max_body_size 1024m;   # файлы до 1 ГБ (по умолчанию в nginx только 1 МБ)

    location / {
        proxy_pass https://qrref;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffering off;
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }
}
```

`server.forward-headers-strategy: framework` (см. `application.yaml`) нужен, чтобы приложение видело настоящий
протокол/хост из `X-Forwarded-*`: иначе проверка Origin для WebSocket отклоняет браузер (403), даже если `url` указан верно.
