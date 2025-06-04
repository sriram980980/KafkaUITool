namespace KafkaTool;

public partial class Form1 : Form
{
    public Form1()
    {
        InitializeComponent();
    }

    private void addClusterToolStripMenuItem_Click(object sender, EventArgs e)
    {
        string input = Microsoft.VisualBasic.Interaction.InputBox(
            "Enter Kafka broker URLs (comma separated):",
            "Add Kafka Cluster",
            "localhost:9092"
        );
        if (!string.IsNullOrWhiteSpace(input))
        {
            // TODO: Store and use the cluster info
            MessageBox.Show($"Cluster added: {input}", "Cluster Added");
        }
    }
}
