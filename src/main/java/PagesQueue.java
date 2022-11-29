import org.wikipedia.Wiki;

import java.util.ArrayList;
import java.util.List;

public class PagesQueue {
    private ArrayList<Page> pages;
    private ArrayList<String> pageNames;
    private Wiki wiki;
    private int pagePointer;
    public PagesQueue(Wiki wiki){
        this.wiki=wiki;
        pages=new ArrayList<>();
        pageNames=new ArrayList<>();
        pagePointer=0;
    }
    private void insertPages() throws Exception{

        int LIMIT= 50;   //
        int start=pagePointer;
        pagePointer+=LIMIT;
         if(pagePointer>pageNames.size())
              pagePointer=pageNames.size();
        if(start>=pagePointer)  // No more pages
            return;
       // int pageNamesLength=pageNames.size();
        List<String> pn= pageNames.subList(start,pagePointer);
       // for (int i=0;pagePointer<pageNamesLength && i<LIMIT;i++,pagePointer++)
        //     pn.add(pageNames.get(pagePointer));
        List<String> ps=wiki.getPageText(pn);
         if(ps.size()!= pn.size()){
             throw  new Exception("Pages Size mismatch");
          }
         Page page;
        for (String p:ps) {
            page=new Page(p);
            page.setTitle(pageNames.get(start++));
            pages.add(page);
        }

    }
    public boolean isEmpty(){
        return pages.size()==0;
    }
    public Page removePage() throws  Exception{
           if(isEmpty())
            insertPages();
        return isEmpty()?null:pages.remove(0);
    }
    public void setPageNames(ArrayList<String> pageNames){
        this.pageNames=pageNames;
    }
    public void setPageNames(List<Wiki.Revision> pageNames){
        for(Wiki.Revision r:pageNames){
            this.pageNames.add(r.getTitle());
        }

    }
}
