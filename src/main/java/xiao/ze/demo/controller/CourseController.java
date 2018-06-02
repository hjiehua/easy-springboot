package xiao.ze.demo.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xiao.ze.demo.entity.Course;
import xiao.ze.demo.service.CourseService;
import xiao.ze.demo.service.CourseTypeService;
import xiao.ze.demo.utils.CourseQueryHelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaozemaliya on 2017/1/31.
 */
@Controller
@RequestMapping("/course")
public class CourseController extends BaseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseTypeService courseTypeService ;

    @ModelAttribute
    public void getCourse(@RequestParam(value="courseNo",required=false) String courseNo,
                          Map<String, Object> map,Course course){

        course=courseService.loadCourseByNo(courseNo);
        if(courseNo != null&&course!= null){
            map.put("course", course);
        }
    }

    @GetMapping("/toInput")
    public String toInput(Map<String, Object> map,Course course) throws Exception {


        map.put("courseTypeList", courseTypeService.loadAll());

        course.setCourseStatus("O");
        course.setCourseReqs(new String[]{"a","b"});

        map.put("course", course);

        return "course/input_course";
    }

    @PostMapping(value="/create")
    public String create(@RequestParam("coursetextbookpic") MultipartFile file, Course course, Map<String, Object> map) throws Exception{

        //读取文件数据，转成字节数组

        if(file!=null){
            course.setCourseTextbookPic(file.getBytes());
        }

        try{
            courseService.addCourse(course);
            System.out.println("你好");
        }catch(Exception e){
            map.put("exceptionMessage", e.getMessage());

            map.put("courseTypeList", courseTypeService.loadAll());

            return "course/input_course";
        }

        return "redirect:/course/list";
    }

    @RequestMapping("/list")
    public String list(@RequestParam(value="pageNo", required=false, defaultValue="1") String pageNoStr,
                       Map<String, Object> map, CourseQueryHelper helper) throws Exception{

        int pageNo = 1;

        //对 pageNo 的校验
        pageNo = Integer.parseInt(pageNoStr);
        if(pageNo < 1){
            pageNo = 1;
        }

        PageHelper.startPage(pageNo, 3);
        List<Course> courselist = courseService.loadScopedCourses(helper);
        PageInfo<Course> page=new PageInfo<Course>(courselist);

        map.put("courseTypeList", courseTypeService.loadAll());
        map.put("page", page);
        map.put("helper", helper);

        return "course/list_course";

    }


    @DeleteMapping(value="/remove/{courseNo}")
    public String remove(@PathVariable("courseNo") String courseNo) throws Exception{

        courseService.removeCourseByNo(courseNo);

        return "redirect:/course/list";

    }

    @GetMapping(value="/preUpdate/{courseNo}")
    public String preUpdate(@PathVariable("courseNo") String courseNo, Map<String, Object> map) throws Exception{

        map.put("course" ,courseService.loadCourseByNo(courseNo));

        map.put("courseTypeList", courseTypeService.loadAll());

        return "course/update_course";

    }

    @PostMapping(value="/update")
    public String update(@RequestParam("coursetextbookpic") MultipartFile file, Course course, Map<String, Object> map) throws Exception{

        //读取多段提交的文件数据，转成字节数组
        if(file.getBytes().length>0){
            course.setCourseTextbookPic(file.getBytes());
        }

        if(course.getCourseTextbookPic()!=null) {
            System.out.println("有图片啊 啊    啊啊 啊啊 啊啊 啊啊啊 ");
        }

        try{
            courseService.updateCourse(course);
        }catch(Exception e){
            map.put("exceptionMessage", e.getMessage());

            map.put("courseTypeList", courseTypeService.loadAll());

            return "/course/update_course";
        }

        return "redirect:/course/list";

    }

    @ResponseBody
    @PostMapping(value="/ajaxValidateCourseNo")
    public String validateCourseNo(@RequestParam(value="courseNo",required=true) String courseNo){

        if(courseService.loadCourseByNo(courseNo) != null){
            return "1";
        }else{
            return "0";
        }

    }

    @GetMapping("/getPic/{courseNo}")
    public String getPic(@PathVariable("courseNo") String courseNo, HttpServletRequest request, HttpServletResponse response) throws Exception{

        byte[] textBookPic = courseService.getTextbookPic(courseNo);

        if(textBookPic==null){
            String path = request.getSession().getServletContext().getRealPath("/pics/default.jpg");
            FileInputStream fis = new FileInputStream(new File(path));

            textBookPic = new byte[fis.available()];
            fis.read(textBookPic);
        }

        //向浏览器发通知，我要发送是图片
        response.setContentType("image/jpeg");
        ServletOutputStream sos=response.getOutputStream();
        sos.write(textBookPic);
        sos.flush();
        sos.close();

        //由于已经把界面数据发回去了，所以不需要struts做VIEW服务了。
        return null;

    }


}