package br.com.easyShop.telas.cadastros;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import utils.data.Data;
import br.com.easyShop.comunicacao.ResultJava;
import br.com.easyShop.model.Cidade;
import br.com.easyShop.model.Contato;
import br.com.easyShop.model.Endereco;
import br.com.easyShop.model.Estado;
import br.com.easyShop.model.Pais;
import br.com.easyShop.model.Pessoa;
import br.com.easyShop.model.PessoaFisica;
import br.com.easyShop.model.PessoaJuridica;
import br.com.easyShop.model.Usuario;
import br.com.easyShop.persistencia.conexao.BancoDeDados;
import br.com.easyShop.service.CidadeService;
import br.com.easyShop.service.ContatoService;
import br.com.easyShop.service.EnderecoService;
import br.com.easyShop.service.EstadoService;
import br.com.easyShop.service.PaisService;
import br.com.easyShop.service.PessoaFisicaService;
import br.com.easyShop.service.PessoaJuridicaService;
import br.com.easyShop.service.PessoaService;
import br.com.easyShop.service.UsuarioService;
import br.com.easyShop.telas.calendario.JDateChooser;
import br.com.easyShop.utils.Constantes;
import br.com.easyShop.utils.TipoContato;
import br.com.easyShop.utils.TipoEndereco;

public class CadastroDeUsuario extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel pnlCadastro;
	private JButton btnCarregarImagem = new JButton("Carregar Imagem");
	private JButton btnCancelar = new JButton("Cancelar");
	private JButton btnLimpar = new JButton("Limpar");
	private final JComboBox cboSexo = new JComboBox();
	private final JDateChooser calendarDataDeNasc = new JDateChooser();
	private JTextField txtUsuario;
	private JTextField txtCPF;
	private JTextField txtApelido;
	private JTextField txtRG;
	private JTextField txtCNPJ;
	private JTextField txtInscricaoEstadual;
	private final DefaultTableModel modelo = new DefaultTableModel();
	private JPasswordField txtPassword;
	private JTextField txtLogin;
	private JTextField txtLogradouro;
	private JTextField txtCEP;
	private JTextField txtBairro;
	private JTextField txtContato;
	private JTextField txtComplemento;
	private JTextField txtNumero;
	private JComboBox cboTipo = new JComboBox();
	private JComboBox cboPais = new JComboBox();
	private JComboBox cboContato = new JComboBox();
	private JComboBox cboCidade = new JComboBox();
	private JComboBox cboEstado = new JComboBox();
	private JButton btnInserirContato = new JButton("");
	private JTable tblContato;
	private Pais pais;
	private Estado estado;
	private Cidade cidade;
	private List<Contato> listaContatos = new ArrayList<Contato>();
	private Contato contato = new Contato();
	private JTextField txtRazao;
	private JTextField txtFantasia;
	private String caminhoImagem;
    private JLabel lblImagem = new JLabel("");
    private BufferedImage imagem_buffered;
    private JButton btnRemover = new JButton("");
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	public CadastroDeUsuario() {

		estado = new Estado();
		pais = new Pais();
		new Cidade();
		btnCarregarImagem.setIcon(new ImageIcon(CadastroDeUsuario.class.getResource("/br/com/easyShop/telas/imagens/aplicacao/Picture.png")));

		btnCarregarImagem.addActionListener(new Abrir());
		btnCancelar.setIcon(new ImageIcon(CadastroDeUsuario.class.getResource("/br/com/easyShop/telas/imagens/aplicacao/Close.png")));
		btnCancelar.addActionListener(new Cancelar());
		btnLimpar.setIcon(new ImageIcon(CadastroDeUsuario.class.getResource("/br/com/easyShop/telas/imagens/aplicacao/Trash.png")));
		btnLimpar.addActionListener(new Limpar());
		btnInserirContato.setIcon(new ImageIcon(CadastroDeUsuario.class.getResource("/br/com/easyShop/telas/imagens/aplicacao/Create.png")));
		btnInserirContato.addActionListener(new Inserir());
		btnRemover.addActionListener(new Remover());

		setTitle("Cadastro de Usu\u00E1rio");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 881, 642);
		pnlCadastro = new JPanel();
		pnlCadastro.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(pnlCadastro);
		pnlCadastro.setLayout(null);

		btnCancelar.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnCancelar.setBounds(684, 480, 160, 41);
		pnlCadastro.add(btnCancelar);

		btnLimpar.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnLimpar.setBounds(684, 406, 160, 41);
		pnlCadastro.add(btnLimpar);

		btnCarregarImagem.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnCarregarImagem.setBounds(677, 217, 177, 41);
		pnlCadastro.add(btnCarregarImagem);

		tabbedPane.setBorder(null);
		tabbedPane.setBounds(21, 21, 636, 180);
		pnlCadastro.add(tabbedPane);

		JPanel panel = new JPanel();
		panel.setBorder(null);
		panel.setBackground(UIManager.getColor("Button.background"));
		tabbedPane.addTab("Pessoa F\u00EDsica", null, panel, null);
		panel.setLayout(null);

		JLabel lblNome = new JLabel("Nome");
		lblNome.setBounds(24, 11, 53, 26);
		lblNome.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(lblNome);

		txtUsuario = new JTextField();
		txtUsuario.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtUsuario.setBounds(78, 11, 527, 26);
		txtUsuario.setColumns(10);
		panel.add(txtUsuario);

		JLabel label_3 = new JLabel("CPF");
		label_3.setBounds(24, 102, 42, 26);
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(label_3);

		txtCPF = new JTextField();
		txtCPF.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtCPF.setBounds(56, 103, 152, 26);
		txtCPF.setColumns(10);
		panel.add(txtCPF);

		JLabel label_5 = new JLabel("Apelido");
		label_5.setBounds(24, 56, 70, 25);
		label_5.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(label_5);

		txtApelido = new JTextField();
		txtApelido.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtApelido.setBounds(90, 56, 227, 26);
		txtApelido.setColumns(10);
		panel.add(txtApelido);

		JLabel label_6 = new JLabel("RG");
		label_6.setBounds(218, 102, 35, 26);
		label_6.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(label_6);

		txtRG = new JTextField();
		txtRG.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtRG.setBounds(247, 103, 165, 26);
		txtRG.setColumns(10);
		panel.add(txtRG);
		cboSexo.setName("");

		cboSexo.setBounds(463, 103, 142, 26);
		cboSexo.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel.add(cboSexo);
		cboSexo.addItem("Masculino");
		cboSexo.addItem("Feminino");

		JLabel label_7 = new JLabel("Sexo");
		label_7.setBounds(422, 102, 59, 26);
		panel.add(label_7);
		label_7.setFont(new Font("Tahoma", Font.PLAIN, 18));

		calendarDataDeNasc.setFont(new Font("Tahoma", Font.PLAIN, 14));
		calendarDataDeNasc.setBounds(453, 56, 152, 26);
		panel.add(calendarDataDeNasc);

		JLabel lblDataDeNasc = new JLabel("Data de Nasc.");
		lblDataDeNasc.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblDataDeNasc.setBounds(335, 55, 118, 26);
		panel.add(lblDataDeNasc);
		//panel.add());

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(UIManager.getColor("Button.background"));
		tabbedPane.addTab("Pessoa Jur\u00EDdica", null, panel_1, null);
		panel_1.setLayout(null);

		JLabel lblCnpj = new JLabel("CNPJ");
		lblCnpj.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblCnpj.setBounds(107, 11, 48, 26);
		panel_1.add(lblCnpj);

		txtCNPJ = new JTextField();
		txtCNPJ.setColumns(10);
		txtCNPJ.setBounds(21, 39, 236, 26);
		panel_1.add(txtCNPJ);

		JLabel lblInscrioEstadual = new JLabel("Inscri\u00E7\u00E3o Estadual");
		lblInscrioEstadual.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblInscrioEstadual.setBounds(57, 76, 152, 27);
		panel_1.add(lblInscrioEstadual);

		txtInscricaoEstadual = new JTextField();
		txtInscricaoEstadual.setColumns(10);
		txtInscricaoEstadual.setBounds(21, 101, 236, 26);
		panel_1.add(txtInscricaoEstadual);

		JLabel lblRazoSocial = new JLabel("Raz\u00E3o Social");
		lblRazoSocial.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblRazoSocial.setBounds(413, 76, 108, 26);
		panel_1.add(lblRazoSocial);

		txtRazao = new JTextField();
		txtRazao.setColumns(10);
		txtRazao.setBounds(331, 101, 270, 26);
		panel_1.add(txtRazao);

		JLabel lblNomeFantasia = new JLabel("Nome Fantasia");
		lblNomeFantasia.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNomeFantasia.setBounds(403, 11, 136, 26);
		panel_1.add(lblNomeFantasia);

		txtFantasia = new JTextField();
		txtFantasia.setColumns(10);
		txtFantasia.setBounds(331, 39, 270, 26);
		panel_1.add(txtFantasia);

		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.setIcon(new ImageIcon(CadastroDeUsuario.class.getResource("/br/com/easyShop/telas/imagens/aplicacao/Save.png")));
		btnSalvar.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnSalvar.setBounds(684, 328, 160, 41);
		pnlCadastro.add(btnSalvar);

		JPanel pnlEndereco = new JPanel();
		pnlEndereco.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlEndereco.setBackground(UIManager.getColor("Button.background"));
		pnlEndereco.setBounds(21, 217, 636, 386);
		pnlCadastro.add(pnlEndereco);
		pnlEndereco.setLayout(null);

		JLabel label_1 = new JLabel("Senha");
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_1.setBounds(157, 349, 70, 26);
		pnlEndereco.add(label_1);

		txtPassword = new JPasswordField();
		txtPassword.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtPassword.setBounds(227, 349, 242, 26);
		pnlEndereco.add(txtPassword);

		JLabel label_2 = new JLabel("Usu\u00E1rio");
		label_2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_2.setBounds(157, 312, 70, 26);
		pnlEndereco.add(label_2);

		txtLogin = new JTextField();
		txtLogin.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtLogin.setColumns(10);
		txtLogin.setBounds(227, 312, 242, 26);
		pnlEndereco.add(txtLogin);

		JLabel label_16 = new JLabel("Logradouro");
		label_16.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_16.setBounds(26, 24, 97, 26);
		pnlEndereco.add(label_16);

		txtLogradouro = new JTextField();
		txtLogradouro.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtLogradouro.setColumns(10);
		txtLogradouro.setBounds(122, 25, 256, 26);
		pnlEndereco.add(txtLogradouro);

		cboTipo.setFont(new Font("Tahoma", Font.PLAIN, 16));
		cboTipo.setBounds(523, 72, 103, 26);
		pnlEndereco.add(cboTipo);
		cboTipo.addItem(Constantes.RESIDENCIA);
		cboTipo.addItem(Constantes.COMERCIAL);
		cboTipo.addItem(Constantes.APARTAMENTO);

		JLabel label_18 = new JLabel("Tipo");
		label_18.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_18.setBounds(484, 71, 42, 26);
		pnlEndereco.add(label_18);

		JLabel label_19 = new JLabel("CEP");
		label_19.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_19.setBounds(484, 24, 48, 26);
		pnlEndereco.add(label_19);

		txtCEP = new JTextField();
		txtCEP.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtCEP.setColumns(10);
		txtCEP.setBounds(516, 25, 110, 26);
		pnlEndereco.add(txtCEP);

		JLabel label_20 = new JLabel("Bairro");
		label_20.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_20.setBounds(26, 72, 59, 26);
		pnlEndereco.add(label_20);

		txtBairro = new JTextField();
		txtBairro.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtBairro.setColumns(10);
		txtBairro.setBounds(80, 72, 165, 26);
		pnlEndereco.add(txtBairro);

		cboPais.setFont(new Font("Tahoma", Font.PLAIN, 16));
		cboPais.setBounds(58, 121, 131, 26);
		pnlEndereco.add(cboPais);

		JLabel label_21 = new JLabel("Pa\u00EDs");
		label_21.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_21.setBounds(26, 121, 42, 26);
		pnlEndereco.add(label_21);

		cboEstado.setFont(new Font("Tahoma", Font.PLAIN, 16));
		cboEstado.setBounds(275, 121, 130, 26);
		pnlEndereco.add(cboEstado);

		JLabel label_22 = new JLabel("Estado");
		label_22.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_22.setBounds(218, 121, 63, 26);
		pnlEndereco.add(label_22);

		cboCidade.setFont(new Font("Tahoma", Font.PLAIN, 16));
		cboCidade.setBounds(474, 121, 152, 26);
		pnlEndereco.add(cboCidade);

		JLabel label_23 = new JLabel("Cidade");
		label_23.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_23.setBounds(415, 121, 59, 26);
		pnlEndereco.add(label_23);

		cboContato.setFont(new Font("Tahoma", Font.PLAIN, 16));
		cboContato.setBounds(91, 171, 131, 26);
		cboContato.addItem(Constantes.CONTATO_TELEFONE);
		cboContato.addItem(Constantes.CONTATO_CELULAR);
		cboContato.addItem(Constantes.CONTATO_EMAIL);
		cboContato.addItem(Constantes.CONTATO_FAX);
		pnlEndereco.add(cboContato);

		JLabel label_24 = new JLabel("Contato");
		label_24.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_24.setBounds(26, 168, 70, 26);
		pnlEndereco.add(label_24);

		btnInserirContato.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnInserirContato.setBounds(536, 171, 35, 33);
		pnlEndereco.add(btnInserirContato);

		txtContato = new JTextField();
		txtContato.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtContato.setColumns(10);
		txtContato.setBounds(232, 171, 294, 26);
		pnlEndereco.add(txtContato);

		JSeparator separator = new JSeparator();
		separator.setBounds(95, 299, 444, 2);
		pnlEndereco.add(separator);

		JLabel label_25 = new JLabel("Complemento");
		label_25.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_25.setBounds(249, 71, 119, 26);
		pnlEndereco.add(label_25);

		txtComplemento = new JTextField();
		txtComplemento.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtComplemento.setColumns(10);
		txtComplemento.setBounds(363, 72, 103, 26);
		pnlEndereco.add(txtComplemento);

		JLabel label_26 = new JLabel("N\u00BA");
		label_26.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_26.setBounds(385, 24, 29, 26);
		pnlEndereco.add(label_26);

		txtNumero = new JTextField();
		txtNumero.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtNumero.setColumns(10);
		txtNumero.setBounds(416, 25, 42, 26);
		pnlEndereco.add(txtNumero);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(139, 208, 348, 80);
		pnlEndereco.add(scrollPane);

	    modelo.addColumn("Tipo");
	    modelo.addColumn("Contato");

		tblContato = new JTable(modelo);
		scrollPane.setViewportView(tblContato);
		tblContato.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		btnRemover.setIcon(new ImageIcon(CadastroDeUsuario.class.getResource("/br/com/easyShop/telas/imagens/aplicacao/Remove.png")));
		btnRemover.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnRemover.setBounds(581, 171, 35, 33);
		pnlEndereco.add(btnRemover);

		lblImagem.setBorder(null);
		lblImagem.setBounds(684, 41, 160, 180);
		pnlCadastro.add(lblImagem);

		btnSalvar.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				PessoaFisicaService pessoaFisicaService = new PessoaFisicaService();
				
				if(verificarCamposVazios()==1){
					String eString = "O(s) campo(s) destacado(s) em Vermelho é(săo) obrigatório(s).";
					JOptionPane.showMessageDialog(null, eString);
				}
				else if(verificarCamposErrados()==1){
						String eString = "O(s) campo(s) destacado(s) Amarelo está(estăo) errado(s).";
						JOptionPane.showMessageDialog(null, eString);
				}
				else{
					Pessoa pessoa = new Pessoa();
					pessoa.setStatus(Constantes.STATUS_ATIVO);
					pessoa.setClientes(null);
	
					if(tabbedPane.getSelectedIndex() == 0){
						PessoaFisica pessoaFisica = new PessoaFisica();
						pessoaFisica.setApelido(txtApelido.getText());
						pessoaFisica.setCpf(txtCPF.getText());
						if (calendarDataDeNasc.getDate() != null) { pessoaFisica.setDataNascimento(new Data(calendarDataDeNasc.getDate())); }
						pessoaFisica.setNome(txtUsuario.getText());
						pessoaFisica.setRg(txtRG.getText());
						pessoaFisica.setSexo(obtemSexo(cboSexo.getSelectedIndex()));
						pessoaFisica.setStatus(Constantes.STATUS_ATIVO);
	
						pessoaFisicaService.inserirPessoaFisica(pessoaFisica);
						
						ResultJava pessoas = pessoaFisicaService.getTodos(pessoaFisica, 0, 0, "pkpessoafisica");
						List<?> treco = new ArrayList<PessoaFisica>();
						treco = pessoas.getLista();
						pessoa.setPessoaFisica((PessoaFisica) treco.get(treco.size()-1));
					}
					else{
						PessoaJuridica pessoaJuridica = new PessoaJuridica();
						pessoaJuridica.setCnpj(txtCNPJ.getText());
						pessoaJuridica.setInscricaoEstadual(txtInscricaoEstadual.getText());
						pessoaJuridica.setNomeFantasia(txtFantasia.getText());
						pessoaJuridica.setRazaoSocial(txtRazao.getText());
						pessoaJuridica.setStatus(Constantes.STATUS_ATIVO);
						
						PessoaJuridicaService pessoaJuridicaService = new PessoaJuridicaService();
						pessoaJuridicaService.inserirPessoaJuridica(pessoaJuridica);
	
						ResultJava pessoas = pessoaFisicaService.getTodos(pessoaJuridica, 0, 0, "pkpessoajuridica");
						List<?> treco = new ArrayList<PessoaJuridica>();
						treco = pessoas.getLista();
						pessoa.setPessoaJuridica((PessoaJuridica) treco.get(treco.size()-1));
					}
	
					PessoaService pessoaService = new PessoaService();
					pessoaService.inserirPessoa(pessoa);
					
					Pessoa pessoa2 = new Pessoa();
					ResultJava pessoas = pessoaFisicaService.getTodos(pessoa, 0, 0, "pkpessoa");
					List<?> treco = new ArrayList<Pessoa>();
					treco = pessoas.getLista();
					pessoa2 = ((Pessoa) treco.get(treco.size()-1));				
					
					if (cboPais.getSelectedItem() != null) { pais = (Pais) cboPais.getSelectedItem(); }
					if (cboEstado.getSelectedItem() != null) { estado = (Estado) cboEstado.getSelectedItem(); }
					estado.setPais(pais);
					if (cboCidade.getSelectedItem() != null) { cidade = (Cidade) cboCidade.getSelectedItem(); }
					if (cidade != null)
					{
						cidade.setEstado(estado);						
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Por favor, Selecione a cidade.");
						return;
					}
	
					Endereco endereco = new Endereco();
					endereco.setBairro(txtBairro.getText());
					endereco.setCep(txtCEP.getText());
					endereco.setCidade(cidade);
					endereco.setLogradouro(txtLogradouro.getText());
					endereco.setPedidos(null);
					endereco.setPessoa(pessoa2);
					endereco.setStatus(Constantes.STATUS_ATIVO);
					endereco.setTipo(TipoEndereco.getIndexTipoEndereco(cboTipo.getSelectedItem().toString()));
					endereco.setComplemento(txtComplemento.getText());
					endereco.setNumero(txtNumero.getText());
	
					Usuario usuario = new Usuario();
					if (txtLogin.getText().length() > 0) { usuario.setLogin(txtLogin.getText()); }
					else { JOptionPane.showMessageDialog(null, "Por favor, digite um usuário"); return; }
					if (txtPassword.getText().length() > 0) { usuario.setSenha(txtPassword.getText()); }
					else {JOptionPane.showMessageDialog(null, "Por favor, digite a senha."); return; }
					usuario.setPessoa(pessoa2);
					usuario.setStatus(Constantes.STATUS_ATIVO);
	
					for(Contato contatoAdd : listaContatos){
						contato = contatoAdd;
						contato.setPessoa(pessoa2);
						ContatoService contatoService = new ContatoService();
						contatoService.salvarContato(contato);
					}
	
					UsuarioService usuarioService = new UsuarioService();
					usuarioService.salvar(usuario);
	
					EnderecoService enderecoService = new EnderecoService();
					enderecoService.salvar(endereco);
	
					//*********************************************************************//
					//Salvar imagem na pasta
					try{
						File imagem_file = new File(caminhoImagem);
						BufferedImage imagem_buffered = null;
						imagem_buffered = ImageIO.read( imagem_file );
						ImageIO.write(imagem_buffered, "jpg", new File("Imagens/ImagensUsuario/usuario"+usuario.getPkUsuario()+".jpg"));
					}catch (Exception e1) {
	
					}
					//*********************************************************************//
	
					JOptionPane.showMessageDialog(null, "Usuário inserido com sucesso!!");
				}
			}
		});

		preencherPais();

		cboPais.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pais = (Pais) cboPais.getSelectedItem();

				EstadoService estadoService = new EstadoService();
				List<Estado> estados = estadoService.getEstados(pais);

				cboEstado.removeAllItems();

                for(Estado estado : estados){
                	cboEstado.addItem(estado);
                }
			}
		});


		cboEstado.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

               estado = (Estado) cboEstado.getSelectedItem();

				CidadeService cidadeService = new CidadeService();
				List<Cidade> cidades = cidadeService.getCidades(estado);

				cboCidade.removeAllItems();

                for(Cidade cidade : cidades){
                	cboCidade.addItem(cidade);
                }
			}
		});

	}

	private String obtemSexo(int sexo){
		if(sexo==0){
			return "masculino";
		}
		else{
			return "feminino";
		}
	}

	private class Abrir implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(CadastroDeUsuario.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				fc.getSelectedFile().toString();

				//*********************************************************************//
				//Salvar imagem na pasta

				try {
				    imagem_buffered = null;
					File imagem_file = new File(fc.getSelectedFile().toString());
					imagem_buffered = ImageIO.read( imagem_file );
					ImageIO.write(imagem_buffered, "jpg", new File("Imagens/ImagensUsuario/CadastroDeUsuario.jpg"));
					lblImagem.setIcon(new ImageIcon(("Imagens/ImagensUsuario/CadastroDeUsuario.jpg")));
					caminhoImagem = fc.getSelectedFile().toString();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Arquivo selecionado năo é uma imagem!");
				}
				//*********************************************************************//

				 BufferedImage aux = new BufferedImage(lblImagem.getSize().width, lblImagem.getSize().height, imagem_buffered.getType());//cria um buffer auxiliar com o tamanho desejado
				 Graphics2D g = aux.createGraphics();//pega a classe graphics do aux para edicao
				 AffineTransform at = AffineTransform.getScaleInstance((double) lblImagem.getSize().width / imagem_buffered.getWidth(), (double) lblImagem.getSize().height / imagem_buffered.getHeight());//cria a transformacao
				 g.drawRenderedImage(imagem_buffered, at);//pinta e transforma a imagem real no auxiliar

				 lblImagem.setIcon(new ImageIcon(aux));
			}
		}
	}

	private class Cancelar implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			BancoDeDados.desconectar();
			CadastroDeUsuario.this.dispose();
		}
	}

	private class Inserir implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if(txtContato.getText().equals("")){
				JOptionPane.showMessageDialog(null, "Digite primeiro o valor do Contato.");
			}else{

				Contato contato = new Contato();
				contato.setContato(txtContato.getText());
				contato.setStatus(Constantes.STATUS_ATIVO);
				contato.setTipo(TipoContato.getIndexTipoContato(cboContato.getSelectedItem().toString()));

				listaContatos.add(contato);

				modelo.addRow(new Object[]{cboContato.getSelectedItem(), txtContato.getText()});
				txtContato.setText("");
			}
		}
	}

	private class Remover implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				modelo.removeRow(tblContato.getSelectedRow());
				listaContatos.remove(tblContato.getSelectedRow());
			} catch (Exception se2) {
				JOptionPane.showMessageDialog(null, "Selecione o Contado que deseja remover da tabela.");
			}
		}
	}

	private class Limpar implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			txtUsuario.setText("");
			txtApelido.setText("");
			calendarDataDeNasc.setCalendar(null);
			txtCPF.setText("");
			txtRG.setText("");
			cboSexo.setSelectedIndex(0);
			txtLogradouro.setText("");
			txtCEP.setText("");
			txtBairro.setText("");
			txtComplemento.setText("");
			cboTipo.setSelectedIndex(0);
			txtContato.setText("");
			txtNumero.setText("");
			txtRazao.setText("");
			txtFantasia.setText("");
			txtCNPJ.setText("");
			txtInscricaoEstadual.setText("");
			txtLogin.setText("");
			txtPassword.setText("");

			int qtd = modelo.getRowCount(), i;
			for(i=0;i<qtd;i++){
				modelo.removeRow(0);
			}
		}
	}

	private void preencherPais(){
		PaisService paisService = new PaisService();
		List<Pais> paises = paisService.getPaises();

		 for(Pais pais : paises){
         	cboPais.addItem(pais);
         }
	}
	
	private void destacaVazio(JTextField text){
		text.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
	}
	
	private void normaliza(JTextField text){
		JTextField jTextField = new JTextField();
		text.setBorder(jTextField.getBorder());
	}
	
	private void destacaErrado(JTextField text){
		text.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
	}
	
	private int verificarCamposErrados(){
		int verificar = 0;
		
		if(tabbedPane.getSelectedIndex() == 0){
			try {@SuppressWarnings("unused")
			int cpf = Integer.parseInt(txtCPF.getText());normaliza(txtCPF);
			} catch (Exception e) {verificar = 1;destacaErrado(txtCPF);}
			
			if(!((""+txtCPF.getText().length()).equals("11"))){	destacaErrado(txtCPF);verificar = 1;}else{verificar = 0;normaliza(txtCPF);}
			
			try {@SuppressWarnings("unused")
			int rg = Integer.parseInt(txtRG.getText());normaliza(txtRG);
			} catch (Exception e) {	verificar = 1;destacaErrado(txtRG);}
			if(!((""+txtRG.getText().length()).equals("9"))){destacaErrado(txtRG);verificar = 1;}else{verificar = 0;normaliza(txtRG);}
		}
		else{
			try {@SuppressWarnings("unused")
			int cnpj = Integer.parseInt(txtCNPJ.getText());normaliza(txtCNPJ);
			} catch (Exception e) {	verificar = 1;destacaErrado(txtCNPJ);}
			if(!((""+txtCNPJ.getText().length()).equals("14"))){destacaErrado(txtCNPJ);verificar = 1;}else{verificar = 0;normaliza(txtCNPJ);}			
			
			try {@SuppressWarnings("unused")
			int cnpj = Integer.parseInt(txtInscricaoEstadual.getText());normaliza(txtInscricaoEstadual);
			} catch (Exception e) {	verificar = 1;destacaErrado(txtInscricaoEstadual);}
			if(!((""+txtInscricaoEstadual.getText().length()).equals("12"))){destacaErrado(txtInscricaoEstadual);verificar = 1;}else{verificar = 0;normaliza(txtInscricaoEstadual);}			
			
		}
		
		try {@SuppressWarnings("unused")
		int numero = Integer.parseInt(txtNumero.getText());normaliza(txtNumero);
		} catch (Exception e) {	verificar = 1;destacaErrado(txtNumero);}
		
		try {@SuppressWarnings("unused")
		int cep = Integer.parseInt(txtCEP.getText());normaliza(txtCEP);
		} catch (Exception e) {	verificar = 1;destacaErrado(txtCEP);}
		if(!((""+txtCEP.getText().length()).equals("8"))){destacaErrado(txtCEP);verificar = 1;}else{verificar = 0;normaliza(txtCEP);}
		
		return verificar;
	}
	@SuppressWarnings("deprecation")
	private int verificarCamposVazios(){
		int verificar = 0;
		
		if(tabbedPane.getSelectedIndex() == 0){
			if(txtUsuario.getText().equals("")){ destacaVazio(txtUsuario); verificar = 1;}else{normaliza(txtUsuario);}
			if(txtApelido.getText().equals("")){ destacaVazio(txtApelido); verificar = 1;}else{normaliza(txtApelido);}
			if(txtCPF.getText().equals("")){ destacaVazio(txtCPF); verificar = 1;}else{normaliza(txtCPF);}
			if(txtRG.getText().equals("")){ destacaVazio(txtRG); verificar = 1;}else{normaliza(txtRG);}
			if(calendarDataDeNasc.getDate()==null){
				calendarDataDeNasc.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			}else{
				JDateChooser jDate = new JDateChooser();
				calendarDataDeNasc.setBorder(jDate.getBorder());
			}
		}
		else{
			if(txtCNPJ.getText().equals("")){ destacaVazio(txtCNPJ); verificar = 1;}else{normaliza(txtCNPJ);}
			if(txtInscricaoEstadual.getText().equals("")){ destacaVazio(txtInscricaoEstadual); verificar = 1;}else{normaliza(txtInscricaoEstadual);}
			if(txtRazao.getText().equals("")){ destacaVazio(txtRazao); verificar = 1;}else{normaliza(txtRazao);}
			if(txtFantasia.getText().equals("")){ destacaVazio(txtFantasia); verificar = 1;}else{normaliza(txtFantasia);}
		}
		if(txtLogradouro.getText().equals("")){ destacaVazio(txtLogradouro); verificar = 1;}else{normaliza(txtLogradouro);}
		if(txtCEP.getText().equals("")){ destacaVazio(txtCEP); verificar = 1;}else{normaliza(txtCEP);}
		if(txtNumero.getText().equals("")){ destacaVazio(txtNumero); verificar = 1;}else{normaliza(txtNumero);}
		//if(txt.getText().equals("")){ destaca(txt); verificar = 1;}else{normaliza(txt);}
		if(txtBairro.getText().equals("")){ destacaVazio(txtBairro); verificar = 1;}else{normaliza(txtBairro);}
		if(txtComplemento.getText().equals("")){ destacaVazio(txtComplemento); verificar = 1;}else{normaliza(txtComplemento);}
		if(txtLogin.getText().equals("")){ destacaVazio(txtLogin); verificar = 1;}else{normaliza(txtLogin);}
		if(txtPassword.getText().equals("")){ destacaVazio(txtPassword); verificar = 1;}else{normaliza(txtPassword);}				
		
		return verificar;
	}
}

