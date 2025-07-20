namespace KafkaTool
{
    partial class Form1 : System.Windows.Forms.Form
    {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem addClusterToolStripMenuItem;
        private System.Windows.Forms.ListBox clustersListBox;
        private System.Windows.Forms.TabControl mainTabControl;

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
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.addClusterToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.clustersListBox = new System.Windows.Forms.ListBox();
            this.mainTabControl = new System.Windows.Forms.TabControl();
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
            // clustersListBox
            // 
            this.clustersListBox.DisplayMember = "Name"; // Show only cluster name
            this.clustersListBox.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.clustersListBox.FormattingEnabled = true;
            this.clustersListBox.Location = new System.Drawing.Point(12, 40);
            this.clustersListBox.Name = "clustersListBox";
            this.clustersListBox.Size = new System.Drawing.Size(150, this.ClientSize.Height - this.clustersListBox.Location.Y - 12); // Fill height
            this.clustersListBox.Anchor = ((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) | System.Windows.Forms.AnchorStyles.Left);
            this.clustersListBox.TabIndex = 1;
            this.clustersListBox.ContextMenuStrip = new System.Windows.Forms.ContextMenuStrip();
            this.clustersListBox.DrawItem += new System.Windows.Forms.DrawItemEventHandler(this.clustersListBox_DrawItem);
            // 
            // mainTabControl
            // 
            this.mainTabControl.Location = new System.Drawing.Point(170, 40);
            this.mainTabControl.Name = "mainTabControl";
            this.mainTabControl.Size = new System.Drawing.Size(600, 380);
            this.mainTabControl.Anchor = ((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) | System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right);
            this.Controls.Add(this.mainTabControl);
            // 
            // Form1
            // 
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.menuStrip1);
            this.Controls.Add(this.clustersListBox);
            this.Controls.Add(this.mainTabControl);
            this.MainMenuStrip = this.menuStrip1;
            this.Text = "KafkaTool";
        }

        #endregion
    }
}
