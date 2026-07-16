import type { LoaderFunctionArgs } from "react-router";
import { redirect, Form, useLoaderData } from "react-router";

import { login } from "../../shopify.server";

import styles from "./styles.module.css";

export const loader = async ({ request }: LoaderFunctionArgs) => {
  const url = new URL(request.url);

  if (url.searchParams.get("shop")) {
    throw redirect(`/app?${url.searchParams.toString()}`);
  }

  return { showForm: Boolean(login) };
};

export default function App() {
  const { showForm } = useLoaderData<typeof loader>();

  return (
    <div className={styles.index}>
      <div className={styles.content}>
        <h1 className={styles.heading}>SupaDatewise: Delivery Date</h1>
        <p className={styles.text}>
          Show shoppers a clear, automatic estimated delivery date on your
          product and cart pages. Install from the Shopify App Store.
        </p>
        {showForm && (
          <Form className={styles.form} method="post" action="/auth/login">
            <label className={styles.label}>
              <span>Shop domain</span>
              <input className={styles.input} type="text" name="shop" />
              <span>e.g: my-shop-domain.myshopify.com</span>
            </label>
            <button className={styles.button} type="submit">
              Log in
            </button>
          </Form>
        )}
        <ul className={styles.list}>
          <li>
            <strong>Automatic estimates</strong>. A get-it-by-date message
            calculated from your processing time, cutoff and shipping window.
          </li>
          <li>
            <strong>No theme code</strong>. Add a drag-and-drop block to your
            product pages in the theme editor.
          </li>
          <li>
            <strong>Fully customizable</strong>. Control the wording, date
            format, working days and locale.
          </li>
        </ul>
      </div>
    </div>
  );
}
