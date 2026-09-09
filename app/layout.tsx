import type {Metadata} from 'next';
import './globals.css';
export const metadata:Metadata={title:'Artifice / Workbench',description:'An evidence-led intake of the Artifice versions, references and next usable build.'};
export default function Layout({children}:{children:React.ReactNode}){return <html lang="en"><body>{children}</body></html>}
