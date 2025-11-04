# AgroMercado Infrastructure with Pulumi

This directory contains the Infrastructure as Code (IaC) for deploying AgroMercado to AWS using Pulumi.

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **AWS CLI** installed and configured
3. **Pulumi CLI** installed ([Install Pulumi](https://www.pulumi.com/docs/get-started/install/))
4. **Node.js** (version 18 or higher)
5. **SSH Key Pair** created in AWS EC2

## Architecture

The infrastructure creates:
- VPC with public subnet
- Internet Gateway
- EC2 Instance (t3.xlarge) with Ubuntu 22.04 LTS
- Security Groups for all required ports
- Elastic IP for stable public access
- Docker and Docker Compose pre-installed

## Setup

### 1. Install Dependencies

```bash
cd infrastructure/pulumi
npm install
```

### 2. Configure AWS Credentials

```bash
aws configure
```

### 3. Create or Select Pulumi Stack

```bash
# Login to Pulumi (use local backend or Pulumi Cloud)
pulumi login

# For local state management:
# pulumi login --local

# Initialize stack
pulumi stack init dev
```

### 4. Configure Stack

Edit `Pulumi.dev.yaml` or set config values:

```bash
# Set your SSH key name (must exist in AWS)
pulumi config set keyName your-aws-key-name

# Optional: Set instance type
pulumi config set instanceType t3.xlarge

# Optional: Restrict SSH access to your IP
pulumi config set allowedSSHIP "YOUR_IP/32"

# Set AWS region
pulumi config set aws:region us-east-1
```

### 5. Preview Infrastructure

```bash
pulumi preview
```

### 6. Deploy Infrastructure

```bash
pulumi up
```

This will:
1. Create all AWS resources
2. Launch EC2 instance
3. Install Docker and Docker Compose
4. Output the public IP and URLs

## Outputs

After deployment, you'll get:

- **Frontend URL**: `http://<PUBLIC_IP>`
- **API Gateway URL**: `http://<PUBLIC_IP>:8080`
- **Eureka Server**: `http://<PUBLIC_IP>:8761`
- **RabbitMQ Management**: `http://<PUBLIC_IP>:15672`
- **SSH Command**: `ssh -i ~/.ssh/<key-name>.pem ubuntu@<PUBLIC_IP>`

## Deploying the Application

### 1. SSH into the EC2 Instance

```bash
ssh -i ~/.ssh/your-key-name.pem ubuntu@<PUBLIC_IP>
```

### 2. Clone Your Repository

```bash
cd /home/ubuntu/agromercado
git clone <your-repo-url> .
```

### 3. Create .env File

Create a `.env` file with the environment variables:

```bash
nano .env
```

Copy from `env.example` and set:
```bash
VITE_API_BASE_URL=http://<PUBLIC_IP>:8080
VITE_GATEWAY_URL=http://<PUBLIC_IP>:8080
# ... other variables
```

### 4. Run Deployment Script

```bash
cd /home/ubuntu/agromercado
bash /home/ubuntu/deploy.sh
```

Or manually:

```bash
docker-compose up -d --build
```

### 5. Check Status

```bash
docker-compose ps
docker-compose logs -f
```

## Updating the Application

To update the application after making changes:

```bash
ssh -i ~/.ssh/your-key-name.pem ubuntu@<PUBLIC_IP>
cd /home/ubuntu/agromercado
git pull
bash /home/ubuntu/deploy.sh
```

## Monitoring

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f frontend
```

### Check Service Health

```bash
# List running containers
docker-compose ps

# Check specific service health
curl http://localhost:8080/actuator/health
```

## Costs Estimation

Approximate monthly costs for `t3.xlarge` instance:
- EC2 Instance: ~$120/month
- EBS Storage (50GB): ~$5/month
- Data Transfer: Variable
- Elastic IP (when associated): Free

**Total**: ~$125-150/month

To reduce costs:
- Use smaller instance type (t3.large: ~$60/month)
- Use reserved instances for long-term savings
- Stop instance when not in use (only pay for storage)

## Cleanup

To destroy all infrastructure:

```bash
pulumi destroy
```

⚠️ **Warning**: This will delete all resources including data!

## Security Recommendations

For production:

1. **Restrict SSH Access**: Set `allowedSSHIP` to your specific IP
2. **Use HTTPS**: Add SSL certificate with Let's Encrypt
3. **Change Default Passwords**: Update all passwords in `.env`
4. **Enable Backups**: Configure automated snapshots
5. **Use Secrets Manager**: Store sensitive data in AWS Secrets Manager
6. **Enable CloudWatch**: Set up monitoring and alerts

## Troubleshooting

### SSH Connection Issues

```bash
# Check instance status
pulumi stack output instanceId
aws ec2 describe-instance-status --instance-ids <instance-id>

# Check security group rules
aws ec2 describe-security-groups --group-ids <sg-id>
```

### Application Not Starting

```bash
# SSH into instance
ssh -i ~/.ssh/your-key-name.pem ubuntu@<PUBLIC_IP>

# Check if setup completed
cat /home/ubuntu/setup-complete.txt

# Check Docker status
sudo systemctl status docker

# View container logs
cd /home/ubuntu/agromercado
docker-compose logs
```

### Database Issues

```bash
# Access PostgreSQL
docker-compose exec postgres psql -U postgres

# Access MongoDB
docker-compose exec mongodb mongosh -u root -p root
```

## Support

For issues or questions, please check:
- [Pulumi Documentation](https://www.pulumi.com/docs/)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [Docker Documentation](https://docs.docker.com/)
