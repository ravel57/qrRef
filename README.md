Web-приложение для перессылки текста на экран, через сканирование QR-кода 

Front-end: https://github.com/ravel57/qrref-front

Запуск Docker:
-
``` 
sudo docker build -t qrref . && sudo docker run --name qrRef -d --restart unless-stopped -p 443:443 --env url=[YOUR_DOMAIN] qrref
```
