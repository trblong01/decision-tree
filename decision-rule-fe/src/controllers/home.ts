import { Request, Response } from "express";

/**
 * Home page.
 * @route GET /
 */

// export const index = (req: Request, res: Response) => {
//     render(<DrawBoard />, rootElement);
// };
export const index = (req: Request, res: Response) => {
    res.render("index", { title: "Home" });
};

export const testing = (req: Request, res: Response) => {
    res.render("test", { title: "Test" });
};