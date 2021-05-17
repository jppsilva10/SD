package AdminConsole.action;

import AdminConsole.model.UserBean;
import com.company.NotFoundException;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;

public class ShareAction  extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String username;
    private String password;

    public String execute() {
        UserBean ub;
        try {
            ub = getUserBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }

        return ub.share();

    }

    public String SetUser(){
        UserBean ub;
        try {
            ub = getUserBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }

        return "success";
    }

    public UserBean getUserBean() throws NotBoundException, RemoteException {
        if(!session.containsKey("UserBean"))
            this.setUserBean(new UserBean());
        return (UserBean) session.get("UserBean");
    }

    public void setUserBean(UserBean ub) {
        this.session.put("UserBean", ub);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
