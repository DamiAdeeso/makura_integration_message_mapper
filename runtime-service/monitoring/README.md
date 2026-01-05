# Monitoring Setup with Prometheus and Grafana

This directory contains configuration files for running Prometheus and Grafana in Docker to monitor the Makura Runtime Service.

## Quick Start

1. **Start the monitoring stack:**
   ```bash
   docker-compose up -d
   ```

2. **Access the services:**
   - **Grafana**: http://localhost:3000
     - Username: `admin`
     - Password: `admin`
   - **Prometheus**: http://localhost:9090

3. **Verify Prometheus is scraping metrics:**
   - Go to http://localhost:9090/targets
   - Check that `makura-runtime-service` target is "UP"

## Prerequisites

- Docker and Docker Compose installed
- Makura Runtime Service running on `localhost:8080`
- The service must expose `/actuator/prometheus` endpoint (already configured)

## Configuration

### Prometheus (`prometheus.yml`)

- Scrapes metrics from `host.docker.internal:8080/actuator/prometheus`
- Scrape interval: 15 seconds
- Data retention: Uses default Prometheus retention

### Grafana

- Pre-configured Prometheus datasource
- Pre-loaded dashboard with translation metrics
- Admin credentials: `admin/admin` (change in production!)

## Accessing Grafana Dashboard

1. Open http://localhost:3000
2. Login with `admin/admin`
3. The "Makura Runtime Service - Translation Metrics" dashboard should be available automatically

## Dashboard Panels

The pre-configured dashboard includes:

1. **Translation Requests per Second** - Rate of requests by route
2. **Translation Success Rate** - Percentage of successful translations
3. **Translation Duration (p95/p50)** - Response time percentiles
4. **Translation Errors by Type** - Error rate breakdown
5. **Total Translation Requests** - Counter stat
6. **Average Translation Duration** - Average response time
7. **Success Rate** - Overall success percentage
8. **Total Errors** - Total error count

## Troubleshooting

### Prometheus can't connect to runtime service

If Prometheus shows the target as "DOWN":

1. **Check the service is running:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Check metrics endpoint:**
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```

3. **On Windows/Mac, `host.docker.internal` should work. On Linux, you may need to:**
   - Use `host.docker.internal` (if Docker Desktop)
   - Or use `172.17.0.1` (default Docker bridge)
   - Or use your host IP address

4. **Update `prometheus.yml` with the correct host:**
   ```yaml
   - targets: ['<your-host-ip>:8080']
   ```

### Grafana shows "No Data"

1. Check Prometheus is scraping: http://localhost:9090/targets
2. Query metrics directly in Prometheus: http://localhost:9090/graph
   - Try: `makura_translation_requests_total`
3. Check Grafana datasource is configured correctly
4. Verify the time range in Grafana includes the time when metrics were collected

### Stopping the services

```bash
docker-compose down
```

To also remove volumes (clears all data):
```bash
docker-compose down -v
```

## Customization

### Change Grafana admin password

Edit `docker-compose.yml`:
```yaml
environment:
  - GF_SECURITY_ADMIN_PASSWORD=your-new-password
```

### Adjust scrape interval

Edit `monitoring/prometheus.yml`:
```yaml
global:
  scrape_interval: 30s  # Change from 15s to 30s
```

### Add more dashboards

1. Export dashboard JSON from Grafana UI
2. Save to `monitoring/grafana-dashboards/`
3. Restart: `docker-compose restart grafana`

## Production Considerations

1. **Change default passwords** - Update Grafana admin password
2. **Use environment variables** - For sensitive configuration
3. **Configure persistent volumes** - Already configured in docker-compose.yml
4. **Set up alerts** - Configure Prometheus alerting rules
5. **Use reverse proxy** - For secure access (nginx/traefik)
6. **Monitor disk usage** - Prometheus data can grow large




