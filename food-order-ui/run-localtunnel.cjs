const localtunnel = require('localtunnel');

(async () => {
  console.log('Starting localtunnel to port 8081...');
  try {
    const tunnel = await localtunnel({ 
      port: 8081, 
      subdomain: 'gourmetflow-backend-arul',
      local_host: '127.0.0.1' 
    });

    console.log('TUNNEL ACTIVE AT:', tunnel.url);

    tunnel.on('close', () => {
      console.log('Tunnel connection closed. Exiting.');
      process.exit(1);
    });

    tunnel.on('error', (err) => {
      console.error('Tunnel error:', err);
      process.exit(1);
    });
  } catch (err) {
    console.error('Failed to start tunnel:', err);
    process.exit(1);
  }
})();
