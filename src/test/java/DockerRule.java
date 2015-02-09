

import java.util.Map;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

public class DockerRule extends ExternalResource {

    public static final String DOCKER_SERVICE_URL = "unix:///var/run/docker.sock";
    private final DockerClient dockerClient;
    private final HostConfig hostConfig;
    private ContainerCreation container;
    private Map<String, String> portBindings;

    public DockerRule(String imageName, String... cmd) {
        this(imageName, DOCKER_SERVICE_URL, cmd);
    }

    public DockerRule(String imageName, String dockerServiceUrl, String... cmd) {
        portBindings = Maps.newHashMap();
        
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(imageName)
                .networkDisabled(false)
                .cmd(cmd)
                .build();

        dockerClient = new DefaultDockerClient(dockerServiceUrl);

        hostConfig = HostConfig.builder()
                .publishAllPorts(true)
                .build();

        try {
            dockerClient.pull(imageName);
            container = dockerClient.createContainer(containerConfig);
        } catch (DockerException | InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return super.apply(base, description);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        dockerClient.startContainer(container.id(), hostConfig);
        portBindings.putAll(extractPortBindings());
    }

    private Map<String, String> extractPortBindings() throws DockerException,
            InterruptedException {
        return Maps.transformValues(
                dockerClient.inspectContainer(container.id())
                .networkSettings()
                .ports(),
                x -> Iterables.getOnlyElement(x).hostPort());
    }

    @Override
    protected void after() {
        super.after();
        try {
            dockerClient.killContainer(container.id());
            dockerClient.removeContainer(container.id(), true);
        } catch (DockerException | InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    public Map<String, String> getPortBindings() {
        return portBindings;
    }

} 