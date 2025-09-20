FROM nginx:stable AS final
EXPOSE 8081
COPY dist/tikito-ui/browser  /usr/share/nginx/html
COPY /nginx.conf  /etc/nginx/conf.d/default.conf
COPY ./src/assets/env.js /usr/share/nginx/html/assets/env.js.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
