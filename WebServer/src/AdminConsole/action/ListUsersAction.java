package AdminConsole.action;

import AdminConsole.model.AdminConsoleBean;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.config.entities.Parameterizable;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ListUsersAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public String execute()
    {
        AdminConsoleBean acb;
        try {
            acb = getAdminConsoleBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }
        return "success";
    }

    public String setUserDetails()
    {
        //String username = params.get("username");
        AdminConsoleBean acb;
        try {
            acb = getAdminConsoleBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }
        return acb.setUserDetails(username);
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
