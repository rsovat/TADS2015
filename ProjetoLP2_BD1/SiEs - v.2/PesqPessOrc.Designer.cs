﻿namespace SiEs___v._2
{
    partial class PesqClien
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
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
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.lstClientes = new System.Windows.Forms.ListView();
            this.cod_ = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.Nome_ = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.Cpf_ = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.btnSelect = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // lstClientes
            // 
            this.lstClientes.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.cod_,
            this.Nome_,
            this.Cpf_});
            this.lstClientes.FullRowSelect = true;
            this.lstClientes.GridLines = true;
            this.lstClientes.Location = new System.Drawing.Point(46, 37);
            this.lstClientes.MultiSelect = false;
            this.lstClientes.Name = "lstClientes";
            this.lstClientes.Size = new System.Drawing.Size(551, 374);
            this.lstClientes.TabIndex = 0;
            this.lstClientes.UseCompatibleStateImageBehavior = false;
            this.lstClientes.View = System.Windows.Forms.View.Details;
            // 
            // cod_
            // 
            this.cod_.Text = "Cod";
            this.cod_.Width = 68;
            // 
            // Nome_
            // 
            this.Nome_.Text = "Nome";
            this.Nome_.Width = 338;
            // 
            // Cpf_
            // 
            this.Cpf_.Text = "CPF";
            this.Cpf_.Width = 138;
            // 
            // btnSelect
            // 
            this.btnSelect.Location = new System.Drawing.Point(320, 430);
            this.btnSelect.Name = "btnSelect";
            this.btnSelect.Size = new System.Drawing.Size(146, 35);
            this.btnSelect.TabIndex = 1;
            this.btnSelect.Text = "Selecionar";
            this.btnSelect.UseVisualStyleBackColor = true;
            this.btnSelect.Click += new System.EventHandler(this.btnSelect_Click);
            // 
            // PesqClien
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(665, 504);
            this.Controls.Add(this.btnSelect);
            this.Controls.Add(this.lstClientes);
            this.Name = "PesqClien";
            this.Text = "PesqClien";
            this.Load += new System.EventHandler(this.PesqClien_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListView lstClientes;
        private System.Windows.Forms.Button btnSelect;
        private System.Windows.Forms.ColumnHeader cod_;
        private System.Windows.Forms.ColumnHeader Nome_;
        private System.Windows.Forms.ColumnHeader Cpf_;
    }
}