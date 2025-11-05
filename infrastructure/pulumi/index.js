const pulumi = require("@pulumi/pulumi");
const aws = require("@pulumi/aws");
const fs = require("fs");

// Configuration
const config = new pulumi.Config();
const instanceType = config.get("instanceType") || "t3.xlarge";
const keyName = config.require("keyName"); // SSH key pair name in AWS
const allowedSSHIP = config.get("allowedSSHIP") || "0.0.0.0/0"; // Restrict SSH access in production

// Get the latest Ubuntu 22.04 LTS AMI
const ubuntuAmi = aws.ec2.getAmi({
    mostRecent: true,
    owners: ["099720109477"], // Canonical
    filters: [
        {
            name: "name",
            values: ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"],
        },
        {
            name: "virtualization-type",
            values: ["hvm"],
        },
    ],
});

// Create VPC
const vpc = new aws.ec2.Vpc("agromercado-vpc", {
    cidrBlock: "10.0.0.0/16",
    enableDnsHostnames: true,
    enableDnsSupport: true,
    tags: {
        Name: "agromercado-vpc",
        Project: "AgroMercado",
    },
});

// Create Internet Gateway
const internetGateway = new aws.ec2.InternetGateway("agromercado-igw", {
    vpcId: vpc.id,
    tags: {
        Name: "agromercado-igw",
        Project: "AgroMercado",
    },
});

// Create Public Subnet
const publicSubnet = new aws.ec2.Subnet("agromercado-public-subnet", {
    vpcId: vpc.id,
    cidrBlock: "10.0.1.0/24",
    availabilityZone: "us-east-2a",
    mapPublicIpOnLaunch: true,
    tags: {
        Name: "agromercado-public-subnet",
        Project: "AgroMercado",
    },
});

// Create Route Table
const publicRouteTable = new aws.ec2.RouteTable("agromercado-public-rt", {
    vpcId: vpc.id,
    routes: [
        {
            cidrBlock: "0.0.0.0/0",
            gatewayId: internetGateway.id,
        },
    ],
    tags: {
        Name: "agromercado-public-rt",
        Project: "AgroMercado",
    },
});

// Associate Route Table with Subnet
const routeTableAssociation = new aws.ec2.RouteTableAssociation("agromercado-rta", {
    subnetId: publicSubnet.id,
    routeTableId: publicRouteTable.id,
});

// Security Group
const securityGroup = new aws.ec2.SecurityGroup("agromercado-sg", {
    vpcId: vpc.id,
    description: "Security group for AgroMercado application",
    ingress: [
        // SSH
        {
            protocol: "tcp",
            fromPort: 22,
            toPort: 22,
            cidrBlocks: [allowedSSHIP],
            description: "SSH access",
        },
        // HTTP (Frontend)
        {
            protocol: "tcp",
            fromPort: 80,
            toPort: 80,
            cidrBlocks: ["0.0.0.0/0"],
            description: "HTTP access for frontend",
        },
        // HTTPS (Optional for future SSL)
        {
            protocol: "tcp",
            fromPort: 443,
            toPort: 443,
            cidrBlocks: ["0.0.0.0/0"],
            description: "HTTPS access for frontend",
        },
        // API Gateway
        {
            protocol: "tcp",
            fromPort: 8080,
            toPort: 8080,
            cidrBlocks: ["0.0.0.0/0"],
            description: "API Gateway",
        },
        // Eureka Server (for debugging, restrict in production)
        {
            protocol: "tcp",
            fromPort: 8761,
            toPort: 8761,
            cidrBlocks: [allowedSSHIP],
            description: "Eureka Server",
        },
        // RabbitMQ Management (for debugging, restrict in production)
        {
            protocol: "tcp",
            fromPort: 15672,
            toPort: 15672,
            cidrBlocks: [allowedSSHIP],
            description: "RabbitMQ Management",
        },
    ],
    egress: [
        {
            protocol: "-1",
            fromPort: 0,
            toPort: 0,
            cidrBlocks: ["0.0.0.0/0"],
            description: "Allow all outbound traffic",
        },
    ],
    tags: {
        Name: "agromercado-sg",
        Project: "AgroMercado",
    },
});

// User data script to install Docker and Docker Compose
const userData = pulumi.interpolate`#!/bin/bash
set -e

# Update system
apt-get update
apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
rm get-docker.sh

# Add ubuntu user to docker group
usermod -aG docker ubuntu

# Install Docker Compose
DOCKER_COMPOSE_VERSION="2.24.0"
curl -L "https://github.com/docker/compose/releases/download/v\${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Create application directory
mkdir -p /home/ubuntu/agromercado
chown -R ubuntu:ubuntu /home/ubuntu/agromercado

# Install Git
apt-get install -y git

# Create deployment script
cat > /home/ubuntu/deploy.sh << 'DEPLOY_SCRIPT'
#!/bin/bash
set -e

echo "====================================="
echo "AgroMercado Deployment Script"
echo "====================================="

cd /home/ubuntu/agromercado || exit 1

# Check if docker-compose.yml exists (indicates repo is cloned)
if [ ! -f "docker-compose.yml" ]; then
    echo "ERROR: docker-compose.yml not found!"
    echo "Please clone your repository to /home/ubuntu/agromercado"
    echo "Example: git clone <your-repo-url> /home/ubuntu/agromercado"
    exit 1
fi

# Pull latest changes if git repo exists
if [ -d ".git" ]; then
    echo "Pulling latest changes..."
    git pull || echo "Warning: git pull failed, continuing with existing code..."
fi

# Stop existing containers
echo "Stopping existing containers..."
docker-compose down || true

# Build and start containers
echo "Building and starting containers..."
docker-compose up -d --build

# Wait a bit for services to start
echo "Waiting for services to start..."
sleep 10

# Show status
echo "====================================="
echo "Deployment complete!"
echo "====================================="
docker-compose ps

echo ""
echo "Application URLs:"
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "localhost")
echo "Frontend: http://${PUBLIC_IP}"
echo "API Gateway: http://${PUBLIC_IP}:8080"
echo "Eureka Server: http://${PUBLIC_IP}:8761"
echo "RabbitMQ Management: http://${PUBLIC_IP}:15672"
DEPLOY_SCRIPT

chmod +x /home/ubuntu/deploy.sh
chown ubuntu:ubuntu /home/ubuntu/deploy.sh

# Enable Docker service
systemctl enable docker
systemctl start docker

# Log completion
echo "Instance setup completed at $(date)" > /home/ubuntu/setup-complete.txt
chown ubuntu:ubuntu /home/ubuntu/setup-complete.txt

echo "Setup complete!"
`;

// Create EC2 Instance
const ec2Instance = new aws.ec2.Instance("agromercado-ec2", {
    instanceType: instanceType,
    ami: ubuntuAmi.then(ami => ami.id),
    subnetId: publicSubnet.id,
    vpcSecurityGroupIds: [securityGroup.id],
    keyName: keyName,
    userData: userData,
    rootBlockDevice: {
        volumeSize: 50, // 50 GB root volume
        volumeType: "gp3",
        deleteOnTermination: true,
    },
    tags: {
        Name: "agromercado-server",
        Project: "AgroMercado",
        Environment: "production",
    },
    // Add user data script that runs on instance creation
    userDataReplaceOnChange: true,
});

// Create Elastic IP for stable public IP
const eip = new aws.ec2.Eip("agromercado-eip", {
    instance: ec2Instance.id,
    vpc: true,
    tags: {
        Name: "agromercado-eip",
        Project: "AgroMercado",
    },
});

// Outputs
exports.instanceId = ec2Instance.id;
exports.publicIp = eip.publicIp;
exports.publicDns = ec2Instance.publicDns;
exports.frontendUrl = pulumi.interpolate`http://${eip.publicIp}`;
exports.apiGatewayUrl = pulumi.interpolate`http://${eip.publicIp}:8080`;
exports.eurekaUrl = pulumi.interpolate`http://${eip.publicIp}:8761`;
exports.rabbitmqManagementUrl = pulumi.interpolate`http://${eip.publicIp}:15672`;
exports.sshCommand = pulumi.interpolate`ssh -i ~/.ssh/${keyName}.pem ubuntu@${eip.publicIp}`;

// Export environment variables for .env file
exports.envVariables = pulumi.interpolate`
# Generated by Pulumi - Add these to your .env file on the EC2 instance
VITE_API_BASE_URL=http://${eip.publicIp}:8080
VITE_GATEWAY_URL=http://${eip.publicIp}:8080
`;
