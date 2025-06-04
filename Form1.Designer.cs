namespace KafkaTool;

partial class Form1
{
    /// <summary>
    ///  Required designer variable.
    /// </summary>
    private System.ComponentModel.IContainer components = null;

    /// <summary>
    ///  Clean up any resources being used.
    /// </summary>
    /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
    protected override void Dispose(bool disposing)
    {
        if (disposing && (components != null))
        {
            components.Dispose();
        }
        base.Dispose(disposing);
    }

    #region Windows Form Designer generated code

    /// <summary>
    ///  Required method for Designer support - do not modify
    ///  the contents of this method with the code editor.
    /// </summary>
    private void InitializeComponent()
    {
        this.components = new System.ComponentModel.Container();
        this.menuStrip1 = new MenuStrip();
        this.fileToolStripMenuItem = new ToolStripMenuItem();
        this.addClusterToolStripMenuItem = new ToolStripMenuItem();
        // 
        // menuStrip1
        // 
        this.menuStrip1.Items.AddRange(new ToolStripItem[] {
            this.fileToolStripMenuItem
        });
        this.menuStrip1.Location = new System.Drawing.Point(0, 0);
        this.menuStrip1.Name = "menuStrip1";
        this.menuStrip1.Size = new System.Drawing.Size(800, 24);
        this.menuStrip1.TabIndex = 0;
        this.menuStrip1.Text = "menuStrip1";
        // 
        // fileToolStripMenuItem
        // 
        this.fileToolStripMenuItem.DropDownItems.AddRange(new ToolStripItem[] {
            this.addClusterToolStripMenuItem
        });
        this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
        this.fileToolStripMenuItem.Text = "File";
        // 
        // addClusterToolStripMenuItem
        // 
        this.addClusterToolStripMenuItem.Name = "addClusterToolStripMenuItem";
        this.addClusterToolStripMenuItem.Text = "Add Cluster";
        this.addClusterToolStripMenuItem.Click += new System.EventHandler(this.addClusterToolStripMenuItem_Click);
        // 
        // Form1
        // 
        this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
        this.ClientSize = new System.Drawing.Size(800, 450);
        this.Controls.Add(this.menuStrip1);
        this.MainMenuStrip = this.menuStrip1;
        this.Text = "KafkaTool";
    }

    #endregion

    private System.Windows.Forms.MenuStrip menuStrip1;
    private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
    private System.Windows.Forms.ToolStripMenuItem addClusterToolStripMenuItem;
}
