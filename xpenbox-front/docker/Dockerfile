FROM nginx:1.27-alpine

RUN apk add --no-cache dumb-init

RUN addgroup -S webgroup && \
    adduser -S webuser -G webgroup

COPY --chown=webuser:webgroup dist/xpenbox-front/browser/ /usr/share/nginx/html/
COPY --chown=webuser:webgroup docker/nginx.conf /etc/nginx/nginx.conf
COPY --chown=webuser:webgroup docker/conf.d/ /etc/nginx/conf.d

RUN chown -R webuser:webgroup /var/cache/nginx && \
    chown -R webuser:webgroup /var/log/nginx && \
    chown -R webuser:webgroup /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown webuser:webgroup /var/run/nginx.pid

USER webuser

EXPOSE 80

ENTRYPOINT ["dumb-init", "--"]
CMD ["nginx", "-g", "daemon off;"]