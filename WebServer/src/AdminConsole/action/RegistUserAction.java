package AdminConsole.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import AdminConsole.model.AdminConsoleBean;

public class RegistUserAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    AdminConsoleBean acb;
    private String tipo=null, nome=null, username=null, password=null, departamento=null, contacto=null, morada=null, numero_CC = null;
    private String dia=null;
    private String mes=null;
    private String ano=null;
    private Calendar validade_CC=null;


    public void setNumero_CC(String numero_CC) {
        this.numero_CC = numero_CC;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public void setValidade_CC(Calendar validade_CC) {
        this.validade_CC = validade_CC;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String execute()
    {
        try {
            acb = getAdminConsoleBean();
        } catch (NotBoundException e) {
            System.out.println("erro1");
            return "fail";
        } catch (RemoteException e) {
            System.out.println("erro2");
            return "fail";
        }

        validade_CC = new GregorianCalendar(Integer.parseInt(dia), Integer.parseInt(mes)-1, Integer.parseInt(ano));
        String str = acb.createPessoa(tipo, nome, username, password, departamento, contacto, morada, numero_CC, validade_CC);

        if(str.equals("success")){
            return "success";
        }
        else{
            return "fail";
        }
    }

    public String setLists(){
        return "success";
    }

    public ArrayList<String> getDaysList()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=1; i<32; i++){
            list.add("" + i);
        }
        return list;
    }

    public ArrayList<String> getMonthsList()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=1; i<13; i++){
            list.add("" + i);
        }
        return list;
    }

    public ArrayList<String> getYearsList()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=2000; i<2031; i++){
            list.add("" + i);
        }
        return list;
    }

    public AdminConsoleBean getAdminConsoleBean() throws NotBoundException, RemoteException
    {
        if(!session.containsKey("AdminConsoleBean"))
            this.setAdminConsoleBean(new AdminConsoleBean());
        return (AdminConsoleBean) session.get("AdminConsoleBean");
    }

    public void setAdminConsoleBean(AdminConsoleBean acBean) {
        this.session.put("AdminConsoleBean", acBean);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}

