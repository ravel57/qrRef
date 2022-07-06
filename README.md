Web-приложение для перессылки текста на экран, через сканирование QR-кода 

Front-end: https://github.com/ravel57/qrref-front

Запуск Docker:
-
``` 
docker build -t qrref . \ 
&& docker run -p 80:80 -p 443:443 --env url=[YOUR_DOMAIN] qrref 
```