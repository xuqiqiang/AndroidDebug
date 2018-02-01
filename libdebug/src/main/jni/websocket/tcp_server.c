#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <errno.h>
#include "tcp_server.h"
#include "platform.h"

int default_port = 11000;

int passive_server(int server_sockfd, int queue) {

    // 设置套接字选项避免地址使用错误
    int on = 1;
    if ((setsockopt(server_sockfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))) < 0) {
        printf("setsockopt : %s\n", strerror(errno));
        close(server_sockfd);
        return ERROR_PORT;
    }

    // 定义sockaddr_in
    struct sockaddr_in server_sockaddr;
    server_sockaddr.sin_family = AF_INET;
    server_sockaddr.sin_addr.s_addr = htonl(INADDR_ANY);

    int port = default_port;
    do {
        port++;
        server_sockaddr.sin_port = htons(port);
    } while (bind(server_sockfd, (struct sockaddr *) &server_sockaddr,
                  sizeof(server_sockaddr)) == -1);

    // listen，成功返回0，出错返回-1
    if (listen(server_sockfd, queue) == -1) {
        printf("listen : %s\n", strerror(errno));
        close(server_sockfd);
        return default_port;
    }
    printf("listen port : %d\n", port);
    return port;
}

