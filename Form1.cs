namespace KafkaTool;

public partial class Form1 : Form
{
    private List<ClusterInfo> clusters = new List<ClusterInfo>();

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
        contextMenu.Items.AddRange(new ToolStripItem[] { editItem, saveItem, removeItem });
        clustersListBox.ContextMenuStrip = contextMenu;
    }

    private void addClusterToolStripMenuItem_Click(object? sender, EventArgs? e)
    {
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
                    MessageBox.Show($"Cluster added: {name} ({brokers})", "Cluster Added");
                }
            }
        }
    }

    private void EditCluster_Click(object? sender, EventArgs? e)
    {
        if (clustersListBox.SelectedIndex >= 0)
        {
            var cluster = (ClusterInfo)clustersListBox.SelectedItem;
            var newName = Microsoft.VisualBasic.Interaction.InputBox(
                "Edit cluster name:",
                "Edit Kafka Cluster Name",
                cluster.Name
            );
            if (string.IsNullOrWhiteSpace(newName)) return;
            var newBrokers = Microsoft.VisualBasic.Interaction.InputBox(
                "Edit broker URLs:",
                "Edit Kafka Cluster Brokers",
                cluster.BrokerUrls
            );
            if (!string.IsNullOrWhiteSpace(newBrokers))
            {
                cluster.Name = newName;
                cluster.BrokerUrls = newBrokers;
                clustersListBox.Items[clustersListBox.SelectedIndex] = cluster;
            }
        }
    }

    private void SaveCluster_Click(object? sender, EventArgs? e)
    {
        // In-memory only for now, but could be extended to persist to file/db
        MessageBox.Show("Clusters saved (in-memory only).", "Save Clusters");
    }

    private void RemoveCluster_Click(object? sender, EventArgs? e)
    {
        if (clustersListBox.SelectedIndex >= 0)
        {
            clusters.RemoveAt(clustersListBox.SelectedIndex);
            clustersListBox.Items.RemoveAt(clustersListBox.SelectedIndex);
        }
    }
}
