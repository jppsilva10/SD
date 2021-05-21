package AdminConsole.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import AdminConsole.model.AdminConsoleBean;


public class CreateElectionAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    AdminConsoleBean acb;

    private String dia_i = null;
    private String mes_i = null;
    private String ano_i = null;
    private String dia_f = null;
    private String mes_f = null;
    private String ano_f = null;
    private String titulo=null, descricao=null, tipo=null;
    private GregorianCalendar inicio, fim;

   public void setTitulo(String titulo){
	this.titulo = titulo;
   }

   public void setDescricao(String descricao){
	this.descricao = descricao;
   }

   public void setTipo(String tipo){
	this.tipo = tipo;
   }

   public void setDiaInicio(String dia_i){
	this.dia_i = dia_i;
   }

   public void setDiaFim(String dia_f){
	this.dia_f = dia_f;
   }

   public void setMesInicio(String mes_i){
	this.mes_i = mes_i;
   }

   public void setMesFim(String mes_f){
	this.mes_f = mes_f;
   }

   public void setAnoInicio(String ano_i){
	this.ano_i = ano_i;
   }

   public void setAnoFim(String ano_f){
	this.ano_f = ano_f;
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
	
	
	inicio = new GregorianCalendar(Integer.parseInt(dia_i), Integer.parseInt(mes_i)-1, Integer.parseInt(ano_i));
	fim = new GregorianCalendar(Integer.parseInt(dia_f), Integer.parseInt(mes_f)-1, Integer.parseInt(ano_f));
	
	String str = acb.createElection(titulo, descricao, tipo, inicio, fim);

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
