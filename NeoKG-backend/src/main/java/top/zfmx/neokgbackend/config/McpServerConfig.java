package top.zfmx.neokgbackend.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.noear.solon.Solon;
import org.noear.solon.ai.chat.tool.MethodToolProvider;
import org.noear.solon.ai.mcp.server.IMcpServerEndpoint;
import org.noear.solon.ai.mcp.server.McpServerEndpointProvider;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.ai.mcp.server.prompt.MethodPromptProvider;
import org.noear.solon.ai.mcp.server.resource.MethodResourceProvider;
import org.noear.solon.web.servlet.SolonServletFilter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;

@Configuration
public class McpServerConfig {
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Autowired
    private List<IMcpServerEndpoint> serverEndpoints;

    @PostConstruct
    public void start() {
        System.setProperty("server.contextPath", contextPath);
        Solon.start(McpServerConfig.class, new String[]{"--cfg=mcpserver.yml"}, app -> {
            //添加全局鉴权过滤器示例（如果不需要，可以删掉）
        });
        springCom2Endpoint();
    }

    @PreDestroy
    public void stop() {
        if (Solon.app() != null) {
            //停止 solon（根据配置，可支持两段式安全停止）
            Solon.stopBlock(false, Solon.cfg().stopDelay());
        }
    }

    //Spring 组件转为端点
    protected void springCom2Endpoint() {
        //提取实现容器里 IMcpServerEndpoint 接口的 bean ，并注册为服务端点
        for (IMcpServerEndpoint serverEndpoint : serverEndpoints) {
            Class<?> serverEndpointClz = AopUtils.getTargetClass(serverEndpoint);
            McpServerEndpoint anno = AnnotationUtils.findAnnotation(serverEndpointClz, McpServerEndpoint.class);

            if (anno == null) {
                continue;
            }

            McpServerEndpointProvider serverEndpointProvider = McpServerEndpointProvider.builder()
                    .from(serverEndpointClz, anno)
                    .build();

            serverEndpointProvider.addTool(new MethodToolProvider(serverEndpointClz, serverEndpoint));
            serverEndpointProvider.addResource(new MethodResourceProvider(serverEndpointClz, serverEndpoint));
            serverEndpointProvider.addPrompt(new MethodPromptProvider(serverEndpointClz, serverEndpoint));

            serverEndpointProvider.postStart();

            //可以再把 serverEndpointProvider 手动转入 SpringBoot 容器
        }
    }

    @Bean
    public FilterRegistrationBean mcpServerFilter() {
        //通过 Servlet Filter 实现 http 能力对接
        FilterRegistrationBean<SolonServletFilter> filter = new FilterRegistrationBean<>();
        filter.setName("SolonFilter");
        filter.addUrlPatterns("/mcp/*");
        filter.setFilter(new SolonServletFilter());
        return filter;
    }
}
