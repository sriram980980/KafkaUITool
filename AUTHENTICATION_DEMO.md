# Kafka UI Tool - Authentication Support Demo

## Enhanced Cluster Dialog

The new cluster dialog now includes comprehensive authentication support:

### Basic Connection Details:
- **Name:** [Text Field] - Cluster Name
- **Broker URLs:** [Text Field] - localhost:9092
- **Connect by default:** [Checkbox]

### Authentication Section:
- **Authentication Type:** [Dropdown] with options:
  - No Authentication (default)
  - SASL Plain
  - SASL SCRAM-SHA-256
  - SASL SCRAM-SHA-512
  - SSL/TLS
  - SASL over SSL
  - Kerberos

### Dynamic Authentication Fields:

#### For SASL Authentication Types:
- **Username:** [Text Field] - Enter username
- **Password:** [Password Field] - Masked input

#### For SSL Authentication:
- **Truststore Location:** [Text Field] - Path to truststore.jks
- **Truststore Password:** [Password Field] - Masked input
- **Keystore Location:** [Text Field] - Path to keystore.jks (optional)
- **Keystore Password:** [Password Field] - Masked input (optional)
- **Key Password:** [Password Field] - Masked input (optional)

#### For Kerberos Authentication:
- **Service Name:** [Text Field] - kafka (default)
- **Realm:** [Text Field] - EXAMPLE.COM
- **Principal:** [Text Field] - Principal name
- **Keytab:** [Text Field] - Path to keytab file (optional)

#### For SASL over SSL:
- Shows both SASL and SSL fields combined

### UI Features:
- Fields dynamically show/hide based on selected authentication type
- Real-time validation with error messages
- Password fields are masked for security
- Clear visual separation between sections
- Scrollable dialog for longer forms
- Proper focus management and tab order

### Validation:
- Required fields are validated per authentication type
- Clear error messages for invalid configurations
- Integration with existing error handling

## Example Usage:

1. **Adding SASL Plain Cluster:**
   - Select "SASL Plain" from authentication dropdown
   - SASL fields appear automatically
   - Enter username and password
   - System validates required fields

2. **Adding SSL Cluster:**
   - Select "SSL/TLS" from authentication dropdown
   - SSL certificate fields appear
   - Configure truststore and optionally keystore
   - Passwords are masked in UI

3. **Editing Existing Cluster:**
   - Authentication type defaults to current setting
   - Existing credentials are loaded (passwords masked)
   - Can change authentication type with form updates

## Security:
- All passwords and sensitive data are encrypted at rest
- UI displays masked credentials (****)
- Actual credentials only decrypted during Kafka connection
- Backward compatible with existing clusters (default to "No Authentication")