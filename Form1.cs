namespace KafkaTool;

using System.Text;
using Confluent.Kafka.Admin;

public partial class Form1 : Form
{
    private List<ClusterInfo> clusters = new List<ClusterInfo>();
    private ToolTip clusterToolTip = new ToolTip();
    private readonly string logFilePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "KafkaTool.log");
    private readonly KafkaService kafkaService = new KafkaService();
    private readonly string clustersFilePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "clusters.json");
    private readonly string lastConnectedFilePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "last_connected.txt");

    public Form1()
    {
        InitializeComponent();
        var contextMenu = new ContextMenuStrip();
        var editItem = new ToolStripMenuItem("Edit");
        var saveItem = new ToolStripMenuItem("Save Changes");
        var removeItem = new ToolStripMenuItem("Remove");
        editItem.Click += EditCluster_Click;
        saveItem.Click += SaveCluster_Click;
        removeItem.Click += RemoveCluster_Click;
        clustersListBox.ContextMenuStrip = contextMenu;
        clustersListBox.MouseMove += clustersListBox_MouseMove;
        clustersListBox.DoubleClick += clustersListBox_DoubleClick;
        Log("Application started");
        LoadClustersFromFile();
        string? lastCluster = LoadLastConnectedCluster();
        if (!string.IsNullOrWhiteSpace(lastCluster))
        {
            var cluster = clusters.FirstOrDefault(c => c.Name == lastCluster);
            if (cluster != null)
            {
                // Connect to the last connected cluster
                clustersListBox.SelectedItem = cluster;
                clustersListBox_DoubleClick(this, EventArgs.Empty);
            }
        }
    }

    private void Log(string message)
    {
        string logEntry = $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {message}";
        Console.WriteLine(logEntry);
        try
        {
            File.AppendAllText(logFilePath, logEntry + Environment.NewLine);
        }
        catch { /* Ignore file errors */ }
    }

    private void SaveClustersToFile()
    {
        try
        {
            var json = Newtonsoft.Json.JsonConvert.SerializeObject(clusters);
            File.WriteAllText(clustersFilePath, json);
        }
        catch (Exception ex)
        {
            Log($"Error saving clusters: {ex.Message}");
        }
    }

    private void LoadClustersFromFile()
    {
        try
        {
            if (File.Exists(clustersFilePath))
            {
                var json = File.ReadAllText(clustersFilePath);
                var loaded = Newtonsoft.Json.JsonConvert.DeserializeObject<List<ClusterInfo>>(json);
                if (loaded != null)
                {
                    clusters = loaded;
                    clustersListBox.Items.Clear();
                    foreach (var c in clusters)
                        clustersListBox.Items.Add(c);
                }
            }
        }
        catch (Exception ex)
        {
            Log($"Error loading clusters: {ex.Message}");
        }
    }

    private void SaveLastConnectedCluster(string clusterName)
    {
        try { File.WriteAllText(lastConnectedFilePath, clusterName); } catch { }
    }

    private string? LoadLastConnectedCluster()
    {
        try
        {
            if (File.Exists(lastConnectedFilePath))
                return File.ReadAllText(lastConnectedFilePath).Trim();
        }
        catch { }
        return null;
    }

    private void addClusterToolStripMenuItem_Click(object? sender, EventArgs? e)
    {
        Log("User action: Add Cluster");
        // Single prompt for both cluster name and broker URIs
        using (var prompt = new Form())
        {
            prompt.Width = 400;
            prompt.Height = 200;
            prompt.Text = "Add Kafka Cluster";

            var lblCluster = new Label() { Left = 10, Top = 20, Text = "Cluster Name", Width = 100 };
            var txtCluster = new TextBox() { Left = 120, Top = 20, Width = 250, Text = "MyCluster" };

            var lblBrokers = new Label() { Left = 10, Top = 60, Text = "Broker URIs", Width = 100 };
            var txtBrokers = new TextBox() { Left = 120, Top = 60, Width = 250, Text = "localhost:9092" };

            var btnOk = new Button() { Text = "OK", Left = 200, Width = 80, Top = 110, DialogResult = DialogResult.OK };
            var btnCancel = new Button() { Text = "Cancel", Left = 290, Width = 80, Top = 110, DialogResult = DialogResult.Cancel };

            btnOk.Click += (s, ev) => { prompt.Close(); };
            btnCancel.Click += (s, ev) => { prompt.Close(); };

            prompt.Controls.Add(lblCluster);
            prompt.Controls.Add(txtCluster);
            prompt.Controls.Add(lblBrokers);
            prompt.Controls.Add(txtBrokers);
            prompt.Controls.Add(btnOk);
            prompt.Controls.Add(btnCancel);

            prompt.AcceptButton = btnOk;
            prompt.CancelButton = btnCancel;
            prompt.StartPosition = FormStartPosition.CenterParent;

            if (prompt.ShowDialog() == DialogResult.OK)
            {
                var name = txtCluster.Text;
                var brokers = txtBrokers.Text;
                if (!string.IsNullOrWhiteSpace(name) && !string.IsNullOrWhiteSpace(brokers))
                {
                    var cluster = new ClusterInfo { Name = name, BrokerUrls = brokers };
                    clusters.Add(cluster);
                    clustersListBox.Items.Add(cluster);
                    SaveClustersToFile();
                }
            }
        }
    }

    private void EditCluster_Click(object? sender, EventArgs? e)
    {
        Log("User action: Edit Cluster");
        if (clustersListBox.SelectedIndex >= 0)
        {
            var cluster = clustersListBox.SelectedItem as ClusterInfo;
            if (cluster == null) return;
            using (var prompt = new Form())
            {
                prompt.Width = 400;
                prompt.Height = 200;
                prompt.Text = "Edit Kafka Cluster";

                var lblCluster = new Label() { Left = 10, Top = 20, Text = "Cluster Name", Width = 100 };
                var txtCluster = new TextBox() { Left = 120, Top = 20, Width = 250, Text = cluster.Name };

                var lblBrokers = new Label() { Left = 10, Top = 60, Text = "Broker URIs", Width = 100 };
                var txtBrokers = new TextBox() { Left = 120, Top = 60, Width = 250, Text = cluster.BrokerUrls };

                var btnOk = new Button() { Text = "OK", Left = 200, Width = 80, Top = 110, DialogResult = DialogResult.OK };
                var btnCancel = new Button() { Text = "Cancel", Left = 290, Width = 80, Top = 110, DialogResult = DialogResult.Cancel };

                btnOk.Click += (s, ev) => { prompt.Close(); };
                btnCancel.Click += (s, ev) => { prompt.Close(); };

                prompt.Controls.Add(lblCluster);
                prompt.Controls.Add(txtCluster);
                prompt.Controls.Add(lblBrokers);
                prompt.Controls.Add(txtBrokers);
                prompt.Controls.Add(btnOk);
                prompt.Controls.Add(btnCancel);

                prompt.AcceptButton = btnOk;
                prompt.CancelButton = btnCancel;
                prompt.StartPosition = FormStartPosition.CenterParent;

                if (prompt.ShowDialog() == DialogResult.OK)
                {
                    var newName = txtCluster.Text;
                    var newBrokers = txtBrokers.Text;
                    if (!string.IsNullOrWhiteSpace(newName) && !string.IsNullOrWhiteSpace(newBrokers))
                    {
                        cluster.Name = newName;
                        cluster.BrokerUrls = newBrokers;
                        clustersListBox.Items[clustersListBox.SelectedIndex] = cluster;
                        SaveClustersToFile();
                    }
                }
            }
        }
    }

    private void SaveCluster_Click(object? sender, EventArgs? e)
    {
        Log("User action: Save Clusters");
        MessageBox.Show("Clusters saved (in-memory only).", "Save Clusters");
    }

    private void RemoveCluster_Click(object? sender, EventArgs? e)
    {
        Log("User action: Remove Cluster");
        if (clustersListBox.SelectedIndex >= 0)
        {
            clusters.RemoveAt(clustersListBox.SelectedIndex);
            clustersListBox.Items.RemoveAt(clustersListBox.SelectedIndex);
            SaveClustersToFile();
        }
    }

    private void clustersListBox_MouseMove(object? sender, MouseEventArgs e)
    {
        int index = clustersListBox.IndexFromPoint(e.Location);
        if (index >= 0 && index < clusters.Count)
        {
            var cluster = (ClusterInfo)clustersListBox.Items[index];
            string tip = cluster.BrokerUrls;
            if (clusterToolTip.GetToolTip(clustersListBox) != tip)
                clusterToolTip.SetToolTip(clustersListBox, tip);
        }
        else
        {
            clusterToolTip.SetToolTip(clustersListBox, "");
        }
    }

    private void clustersListBox_DrawItem(object? sender, DrawItemEventArgs e)
    {
        try
        {
            if (e.Index < 0 || e.Index >= clustersListBox.Items.Count)
                return;
            var cluster = (ClusterInfo)clustersListBox.Items[e.Index];
            Color textColor = Color.Black;
            Image? icon = null;
            string statusText = string.Empty;
            if (cluster.Status == "Connecting")
            {
                textColor = Color.Goldenrod;
                icon = SystemIcons.Question.ToBitmap();
                statusText = " - connecting";
            }
            else if (cluster.Status == "Connected")
            {
                textColor = Color.Green;
                icon = SystemIcons.Shield.ToBitmap();
                statusText = " - connected";
            }
            else
            {
                switch (cluster.Status)
                {
                    case "Failed":
                        textColor = Color.Red;
                        icon = SystemIcons.Error.ToBitmap();
                        break;
                    default:
                        textColor = Color.Black;
                        icon = SystemIcons.Information.ToBitmap();
                        break;
                }
            }
            // Draw background (transparent for selection)
            if ((e.State & DrawItemState.Selected) == DrawItemState.Selected)
            {
                e.Graphics.FillRectangle(SystemBrushes.Window, e.Bounds); // No blue background
                using (var borderPen = new Pen(Color.Blue, 2))
                {
                    Rectangle borderRect = new Rectangle(e.Bounds.Left, e.Bounds.Top, e.Bounds.Width - 1, e.Bounds.Height - 1);
                    e.Graphics.DrawRectangle(borderPen, borderRect);
                }
            }
            else
            {
                e.DrawBackground();
            }
            int iconSize = e.Bounds.Height - 4;
            if (icon != null)
                e.Graphics.DrawImage(icon, e.Bounds.Left + 2, e.Bounds.Top + 2, iconSize, iconSize);
            using (Brush brush = new SolidBrush(textColor))
            {
                var displayText = cluster.Name + statusText;
                e.Graphics.DrawString(displayText, e.Font ?? SystemFonts.DefaultFont, brush, e.Bounds.Left + iconSize + 6, e.Bounds.Top + 2);
            }
            // No default focus rectangle
        }
        catch (Exception ex)
        {
            Log($"Error in DrawItem: {ex.Message}");
        }
    }

    private Task<List<string>> GetTopicsAsync(string brokerUrls)
    {
        return Task.Run(() => {
            try
            {
                var config = new Confluent.Kafka.AdminClientConfig { BootstrapServers = brokerUrls };
                using (var adminClient = new Confluent.Kafka.AdminClientBuilder(config).Build())
                {
                    var meta = adminClient.GetMetadata(TimeSpan.FromSeconds(5));
                    return meta.Topics.Select(t => t.Topic).ToList();
                }
            }
            catch (Exception ex)
            {
                Log($"Error fetching topics: {ex.Message}");
                return new List<string>();
            }
        });
    }

    private async void OpenClusterTab(ClusterInfo cluster)
    {
        // Check if a tab for this cluster already exists
        foreach (TabPage tab in mainTabControl.TabPages)
        {
            if (tab.Text == cluster.Name)
            {
                mainTabControl.SelectedTab = tab;
                return;
            }
        }
        // Create new tab for the cluster
        var newTab = new TabPage(cluster.Name);
        // Create sub TabControl for Topics, Consumers, Brokers
        var subTabControl = new TabControl();
        subTabControl.Dock = DockStyle.Fill;
        // Topics tab (default)
        var topicsTab = new TabPage("Topics");
        topicsTab.Padding = new Padding(6);
        // Add a horizontal FlowLayoutPanel for the topic action buttons
        var topicButtonPanel = new FlowLayoutPanel {
            Dock = DockStyle.Top,
            Height = 52, // Fix: ensure enough height for buttons
            FlowDirection = FlowDirection.LeftToRight,
            WrapContents = false,
            Padding = new Padding(8, 8, 0, 8), // Fix: add padding for visual clarity
            Margin = new Padding(0, 0, 0, 0),
            AutoSize = false
        };
        // Create the buttons
        var deleteTopicButton = new Button { Text = "Delete", Width = 90, Height = 36, Enabled = false, Margin = new Padding(0, 0, 8, 0) };
        var editTopicButton = new Button { Text = "Edit", Width = 90, Height = 36, Enabled = false, Margin = new Padding(0, 0, 8, 0) };
        var createTopicButton = new Button { Width = 90, Height = 36, Margin = new Padding(0, 0, 0, 0) };
        // Custom paint for rotated text
        createTopicButton.Paint += (s, e) => {
            var btn = (Button)s!;
            e.Graphics.TranslateTransform(btn.Width / 2, btn.Height / 2);
            var textSize = e.Graphics.MeasureString("Create Topic", btn.Font);
             e.Graphics.DrawString("Create Topic", btn.Font, new SolidBrush(btn.ForeColor), -textSize.Width / 2, -textSize.Height / 2);
            e.Graphics.ResetTransform();
        };
        createTopicButton.Text = string.Empty;
        topicButtonPanel.Controls.Add(deleteTopicButton);
        topicButtonPanel.Controls.Add(editTopicButton);
        topicButtonPanel.Controls.Add(createTopicButton);
        topicsTab.Controls.Add(topicButtonPanel);
        // Create a vertical TabControl for topics
        var topicsVerticalTabs = new TabControl { Dock = DockStyle.Fill, Alignment = TabAlignment.Left, SizeMode = TabSizeMode.FillToRight, ItemSize = new Size(30, 120), DrawMode = TabDrawMode.OwnerDrawFixed };
        topicsVerticalTabs.DrawItem += (s, e) =>
        {
            Log("Drawing topic tab item"+e.Index);
            if (s is not TabControl tabControl) return;
            if (e.Index < 0 || e.Index >= tabControl.TabPages.Count) return;
            var tabPage = tabControl.TabPages[e.Index];
            if (tabPage == null) return;
            var g = e.Graphics;
            g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.AntiAlias;
            Rectangle tabBounds = tabControl.GetTabRect(e.Index);
            // Draw background
            using (Brush backBrush = new SolidBrush(e.State.HasFlag(DrawItemState.Selected) ? Color.LightBlue : SystemColors.Control))
                g.FillRectangle(backBrush, tabBounds);
            // Draw border
            using (Pen borderPen = new Pen(Color.Gray))
                g.DrawRectangle(borderPen, tabBounds);
            // Draw tab text rotated 90 deg right (clockwise)
            string text = tabPage.Text;
            using (var font = new Font(tabControl.Font.FontFamily, tabControl.Font.Size, FontStyle.Bold))
            using (var brush = new SolidBrush(Color.Black))
            {
                g.TranslateTransform(tabBounds.Left + tabBounds.Width / 2, tabBounds.Top + tabBounds.Height / 2);
                SizeF textSize = g.MeasureString(text, font);
                g.DrawString(text, font, brush, -textSize.Width / 2, -textSize.Height / 2);
                g.ResetTransform();
            }
        };
        topicsTab.Controls.Add(topicsVerticalTabs);
        topicsTab.Controls.SetChildIndex(topicsVerticalTabs, 1); // Ensure topicsVerticalTabs is below the button panel
        subTabControl.TabPages.Add(topicsTab);
        // Consumers tab
        var consumersTab = new TabPage("Consumers");
        subTabControl.TabPages.Add(consumersTab);
        // Brokers tab
        var brokersTab = new TabPage("Brokers");
        subTabControl.TabPages.Add(brokersTab);
        newTab.Controls.Add(subTabControl);
        mainTabControl.TabPages.Add(newTab);
        mainTabControl.SelectedTab = newTab;
        subTabControl.SelectedTab = topicsTab;
        // Load topics asynchronously
        var topics = await GetTopicsAsync(cluster.BrokerUrls);
        topicsVerticalTabs.TabPages.Clear();
        
            var topicTab1 = new TabPage("");
            // You can add more controls/info to each topicTab here
            topicsVerticalTabs.TabPages.Add(topicTab1);

        foreach (var topic in topics)
        {
            var topicTab = new TabPage(topic);
            // You can add more controls/info to each topicTab here
            topicsVerticalTabs.TabPages.Add(topicTab);
        }
        // Create Topic logic
        createTopicButton.Click += async (s, e) =>
        {
            using (var prompt = new Form())
            {
                prompt.Width = 400;
                prompt.Height = 320;
                prompt.Text = "Create New Topic";
                var lblName = new Label() { Left = 10, Top = 20, Text = "Topic Name", Width = 100 };
                var txtName = new TextBox() { Left = 120, Top = 20, Width = 200 };
                var lblPartitions = new Label() { Left = 10, Top = 60, Text = "Partitions", Width = 100 };
                var numPartitions = new NumericUpDown() { Left = 120, Top = 60, Width = 60, Minimum = 1, Maximum = 100, Value = 1 };
                var lblReplicas = new Label() { Left = 10, Top = 100, Text = "Replicas", Width = 100 };
                var numReplicas = new NumericUpDown() { Left = 120, Top = 100, Width = 60, Minimum = 1, Maximum = 10, Value = 1 };
                var lblProps = new Label() { Left = 10, Top = 140, Text = "Topic Properties like  compression.type=producer (multi line)", Width = 120 };
                var txtProps = new TextBox() {
                    Left = 10, Top = 165, Width = 360, Height = 60,
                    Multiline = true, ScrollBars = ScrollBars.Vertical,
                    Text = string.Empty // No default value
                };
                // Prevent Enter key from submitting the form
                txtProps.KeyDown += (sender2, e2) => {
                    if (e2.KeyCode == Keys.Enter)
                    {
                        e2.SuppressKeyPress = true; // Allow newline in textbox
                        txtProps.AppendText(Environment.NewLine); // Add newline
                    }
                };
                var btnOk = new Button() { Text = "Create", Left = 120, Width = 80, Top = 240, DialogResult = DialogResult.OK };
                var btnCancel = new Button() { Text = "Cancel", Left = 210, Width = 80, Top = 240, DialogResult = DialogResult.Cancel };
                prompt.Controls.Add(lblName);
                prompt.Controls.Add(txtName);
                prompt.Controls.Add(lblPartitions);
                prompt.Controls.Add(numPartitions);
                prompt.Controls.Add(lblReplicas);
                prompt.Controls.Add(numReplicas);
                prompt.Controls.Add(lblProps);
                prompt.Controls.Add(txtProps);
                prompt.Controls.Add(btnOk);
                prompt.Controls.Add(btnCancel);
                prompt.AcceptButton = btnOk;
                prompt.CancelButton = btnCancel;
                prompt.StartPosition = FormStartPosition.CenterParent;
                // Set placeholder/cue banner for topic properties (WinForms hack)
                // try {
                //     NativeMethods.SetCueBanner(txtProps, "compression.type=producer");
                // } catch { /* ignore if fails */ }
                if (prompt.ShowDialog() == DialogResult.OK)
                {
                    string topicName = txtName.Text.Trim();
                    int partitions = (int)numPartitions.Value;
                    short replicas = (short)numReplicas.Value;
                    string propsText = txtProps.Text;
                    var propsDict = new Dictionary<string, string>();
                    var lines = propsText.Split(new[] { "\r\n", "\n", "\r" }, StringSplitOptions.RemoveEmptyEntries);
                    foreach (var line in lines)
                    {
                        var trimmed = line.Trim();
                        if (string.IsNullOrWhiteSpace(trimmed) || !trimmed.Contains("=")) continue;
                        var idx = trimmed.IndexOf('=');
                        var key = trimmed.Substring(0, idx).Trim();
                        var value = trimmed.Substring(idx + 1).Trim();
                        if (!string.IsNullOrEmpty(key)) propsDict[key] = value;
                    }
                    if (!string.IsNullOrWhiteSpace(topicName))
                    {
                        var (created, errorMsg) = await CreateTopicAsyncWithError(cluster.BrokerUrls, topicName, partitions, replicas, propsDict);
                        if (created)
                        {
                            Log($"Created topic '{topicName}'");
                            var topicTab = new TabPage(topicName);
                            topicsVerticalTabs.TabPages.Add(topicTab);
                            topicsVerticalTabs.SelectedTab = topicTab;
                        }
                        else
                        {
                            MessageBox.Show($"Failed to create topic.\n{errorMsg}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        }
                    }
                }
            }
        };
        // --- Button alignment and enable/disable logic ---
        deleteTopicButton.Width = editTopicButton.Width = createTopicButton.Width = 90;
        deleteTopicButton.Height = editTopicButton.Height = createTopicButton.Height = 36;
        deleteTopicButton.Margin = new Padding(0, 0, 8, 0);
        editTopicButton.Margin = new Padding(0, 0, 8, 0);
        createTopicButton.Margin = new Padding(0, 0, 0, 0);
        topicButtonPanel.Padding = new Padding(8, 8, 0, 8);
        topicButtonPanel.Height = 52;
        // Enable/disable logic for Delete/Edit
        void UpdateTopicButtons() {
            bool hasSelection = topicsVerticalTabs.SelectedTab != null && topicsVerticalTabs.SelectedTab.Text.Length> 0;
            editTopicButton.Enabled = hasSelection;
            deleteTopicButton.Enabled = hasSelection;
        }
        // Remove previous handler if needed (safe pattern)
        topicsVerticalTabs.SelectedIndexChanged -= (s, e) => UpdateTopicButtons();
        topicsVerticalTabs.SelectedIndexChanged += (s, e) => UpdateTopicButtons();
        UpdateTopicButtons();
        // Remove previous event handlers to avoid multiple registrations
        editTopicButton.Click -= EditTopicHandler;
        deleteTopicButton.Click -= DeleteTopicHandler;
        // Register handlers only once per cluster tab
        editTopicButton.Click += EditTopicHandler;
        deleteTopicButton.Click += DeleteTopicHandler;
        // Handler methods
        async void EditTopicHandler(object? s, EventArgs e)
        {
            var selectedTab = topicsVerticalTabs.SelectedTab;
            if (selectedTab == null) return;
            // Fetch current topic config from KafkaService
            var currentConfig = new Dictionary<string, string>();
            try {
                var fetched = await kafkaService.GetTopicConfigAsync(cluster.BrokerUrls, selectedTab.Text);
                if (fetched != null) currentConfig = fetched;
            } catch (Exception ex) {
                Log($"Error fetching topic config: {ex.Message}");
                MessageBox.Show($"Failed to fetch topic config: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }
            using (var prompt = new Form())
            {
                prompt.Width = 400;
                prompt.Height = 250;
                prompt.Text = $"Edit Topic: {selectedTab.Text}";
                var lblProps = new Label() { Left = 10, Top = 20, Text = "Topic Properties (multi line)", Width = 200 };
                var txtProps = new TextBox() {
                    Left = 10, Top = 50, Width = 360, Height = 80,
                    Multiline = true, ScrollBars = ScrollBars.Vertical,
                    Text = currentConfig != null ? string.Join("\r\n", currentConfig.Select(kv => $"{kv.Key}={kv.Value}")) : string.Empty
                };
                // try { NativeMethods.SetCueBanner(txtProps, "compression.type=producer"); } catch { }
                var btnOk = new Button() { Text = "Save", Left = 120, Width = 80, Top = 150, DialogResult = DialogResult.OK };
                var btnCancel = new Button() { Text = "Cancel", Left = 210, Width = 80, Top = 150, DialogResult = DialogResult.Cancel };
                prompt.Controls.Add(lblProps);
                prompt.Controls.Add(txtProps);
                prompt.Controls.Add(btnOk);
                prompt.Controls.Add(btnCancel);
                prompt.AcceptButton = btnOk;
                prompt.CancelButton = btnCancel;
                prompt.StartPosition = FormStartPosition.CenterParent;
                if (prompt.ShowDialog() == DialogResult.OK)
                {
                    string propsText = txtProps.Text;
                    var propsDict = new Dictionary<string, string>();
                    var lines = propsText.Split(new[] { "\r\n", "\n", "\r" }, StringSplitOptions.RemoveEmptyEntries);
                    foreach (var line in lines)
                    {
                        var trimmed = line.Trim();
                        if (string.IsNullOrWhiteSpace(trimmed) || !trimmed.Contains("=")) continue;
                        var idx = trimmed.IndexOf('=');
                        var key = trimmed.Substring(0, idx).Trim();
                        var value = trimmed.Substring(idx + 1).Trim();
                        if (!string.IsNullOrEmpty(key)) propsDict[key] = value;
                    }
                    try {
                        await kafkaService.AlterTopicConfigAsync(cluster.BrokerUrls, selectedTab.Text, propsDict);
                        Log($"Edited topic '{selectedTab.Text}' with properties: {string.Join(", ", propsDict.Select(kv => kv.Key + "=" + kv.Value))}");
                        MessageBox.Show("Topic properties updated successfully.", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    } catch (Exception ex) {
                        Log($"Error editing topic: {ex.Message}");
                        MessageBox.Show($"Failed to update topic: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }
        }
        async void DeleteTopicHandler(object? s, EventArgs e)
        {
            var selectedTab = topicsVerticalTabs.SelectedTab;
            if (selectedTab == null) return;
            var result = MessageBox.Show($"Are you sure you want to delete topic '{selectedTab.Text}'?", "Confirm Delete", MessageBoxButtons.YesNo, MessageBoxIcon.Warning);
            if (result == DialogResult.Yes)
            {
                try {
                    await kafkaService.DeleteTopicAsync(cluster.BrokerUrls, selectedTab.Text);
                    Log($"Deleted topic '{selectedTab.Text}'");
                    topicsVerticalTabs.TabPages.Remove(selectedTab);
                    MessageBox.Show("Topic deleted successfully.", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);
                } catch (Exception ex) {
                    Log($"Error deleting topic: {ex.Message}");
                    MessageBox.Show($"Failed to delete topic: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
        }
        // Move createTopicButton to the right
        createTopicButton.Left = 230;
        createTopicButton.Top = 0;
        createTopicButton.Anchor = deleteTopicButton.Anchor = editTopicButton.Anchor = AnchorStyles.Top | AnchorStyles.Left;
        // Enable/disable edit/delete based on selection
        topicsVerticalTabs.SelectedIndexChanged += (s, e) => {
            bool topicSelected = topicsVerticalTabs.SelectedIndex >= 0 && topicsVerticalTabs.TabPages.Count > 0;
            editTopicButton.Enabled = deleteTopicButton.Enabled = topicSelected;
        };
        // Draw icon before selected tab text
        topicsVerticalTabs.DrawItem += (s, e) =>
        {
            if (s is not TabControl tabControl) return;
            if (e.Index < 0 || e.Index >= tabControl.TabPages.Count) return;
            var tabPage = tabControl.TabPages[e.Index];
            if (tabPage == null) return;
            var g = e.Graphics;
            g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.AntiAlias;
            Rectangle tabBounds = tabControl.GetTabRect(e.Index);
            // Draw background
            using (Brush backBrush = new SolidBrush(e.State.HasFlag(DrawItemState.Selected) ? Color.LightBlue : SystemColors.Control))
                g.FillRectangle(backBrush, tabBounds);
            // Draw border
            using (Pen borderPen = new Pen(Color.Gray))
                g.DrawRectangle(borderPen, tabBounds);
            // Draw icon before selected tab text
            int iconOffset = 0;
            if (e.State.HasFlag(DrawItemState.Selected))
            {
                var icon = SystemIcons.Information.ToBitmap();
                int iconSize = 16;
                g.DrawImage(icon, tabBounds.Left + 6, tabBounds.Top + (tabBounds.Height - iconSize) / 2, iconSize, iconSize);
                iconOffset = iconSize + 8;
            }
            // Draw tab text rotated 90 deg right (clockwise)
            string text = tabPage.Text;
            using (var font = new Font(tabControl.Font.FontFamily, tabControl.Font.Size, FontStyle.Bold))
            using (var brush = new SolidBrush(Color.Black))
            {
                g.TranslateTransform(tabBounds.Left + tabBounds.Width / 2 + iconOffset, tabBounds.Top + tabBounds.Height / 2);
                SizeF textSize = g.MeasureString(text, font);
                g.DrawString(text, font, brush, -textSize.Width / 2, -textSize.Height / 2);
                g.ResetTransform();
            }
        };
    }

    // New method to return error message
    private async Task<(bool, string)> CreateTopicAsyncWithError(string brokerUrls, string topicName, int partitions, short replicas, Dictionary<string, string>? config = null)
    {
        try
        {
            var adminConfig = new Confluent.Kafka.AdminClientConfig { BootstrapServers = brokerUrls };
            using (var adminClient = new Confluent.Kafka.AdminClientBuilder(adminConfig).Build())
            {
                var spec = new TopicSpecification {
                    Name = topicName,
                    NumPartitions = partitions,
                    ReplicationFactor = replicas,
                    Configs = config ?? new Dictionary<string, string>()
                };
                await adminClient.CreateTopicsAsync(new[] { spec });
                return (true, string.Empty);
            }
        }
        catch (Exception ex)
        {
            Log($"Error creating topic: {ex.Message}");
            return (false, ex.Message);
        }
    }

    private async void clustersListBox_DoubleClick(object? sender, EventArgs e)
    {
        Log("User action: Double-click cluster to connect");
        try
        {
            if (clustersListBox.SelectedIndex < 0)
                return;
            var cluster = clustersListBox.SelectedItem as ClusterInfo;
            if (cluster == null)
                return;
            // Check if already connected
            if (cluster.Status == "Connected")
            {
                MessageBox.Show("Already connected to this cluster.", "Info", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }
            cluster.Status = "Connecting";
            clustersListBox.Refresh();
            await Task.Delay(500);
            cluster.Status = "Connected";
            Log($"Connected to cluster: {cluster.Name}");
            clustersListBox.Refresh();
            SaveLastConnectedCluster(cluster.Name);
            // Open cluster tab
            OpenClusterTab(cluster);
        }
        catch (Exception ex)
        {
            Log($"Error connecting to cluster: {ex.Message}");
            MessageBox.Show($"Error connecting to cluster: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            if (clustersListBox.SelectedIndex >= 0)
            {
                var cluster = clustersListBox.SelectedItem as ClusterInfo;
                if (cluster != null)
                {
                    cluster.Status = "Failed";
                    clustersListBox.Refresh();
                }
            }
        }
    }
}
