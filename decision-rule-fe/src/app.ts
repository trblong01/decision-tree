import express from "express";
import compression from "compression";  // compresses requests
import bodyParser from "body-parser";
import flash from "express-flash";
import path from "path";
import session from "express-session";
import cors from "cors";

// Controllers (route handlers)
import * as homeController from "./controllers/home";

// API keys and Passport configuration
import * as ejs from "ejs";

// Create Express server
const app = express();

const corsOptions ={
    origin:"*", 
    // credentials: true,
    methods: "GET,HEAD,OPTIONS,PUT,PATCH,POST,DELETE",
  
     optionSuccessStatus:200,
  };
  
app.use(cors(corsOptions)); // Use this after the variable declaration
// Express configuration
app.set("port", process.env.PORT || 3000);

// app.set("views", path.join(__dirname, "../views"));
// app.set("view engine", "html");
app.use(express.static(path.join(__dirname + "/public")));
app.use(express.static(path.join(__dirname + "/public/images")));
app.use(express.static(path.join(__dirname + "/public/fonts")));
app.set("views", path.join(__dirname, "../views"));
app.engine("html",ejs.renderFile);
app.set("view engine", "html");

app.use(compression());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
// app.use(session({
//     resave: true,
//     saveUninitialized: true,
//     secret: "SESSION_SECRET"
// }));
app.use( session( { secret: "ssi vn",
                    cookie: { maxAge: 60*60000 },
                    rolling: true,
                    resave: true, 
                    saveUninitialized: false
                  }
         )
);

app.use(flash());




/**
 * Primary app routes.
 */
app.get("/", homeController.index);
app.get("/home", homeController.index);
app.get("/test", homeController.testing);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       

export default app;
