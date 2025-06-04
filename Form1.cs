namespace KafkaTool;

using System.Text;

public partial class Form1 : Form
{
    private List<ClusterInfo> clusters = new List<ClusterInfo>();
    private ToolTip clusterToolTip = new ToolTip();
    private readonly string logFilePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "KafkaTool.log");
    private readonly KafkaService kafkaService = new KafkaService();

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
            // Set status to Connecting and refresh UI
            cluster.Status = "Connecting";
            clustersListBox.Enabled = false;
            clustersListBox.Refresh();
            // Use real KafkaService connection logic
            bool connected = await kafkaService.ConnectAsync(cluster.BrokerUrls);
            cluster.Status = connected ? "Connected" : "Failed";
            Log($"Cluster '{cluster.Name}' connection result: {cluster.Status}");
            clustersListBox.Enabled = true;
            clustersListBox.Refresh();
        }
        catch (Exception ex)
        {
            Log($"Error during cluster connect: {ex.Message}");
        }
    }
}
