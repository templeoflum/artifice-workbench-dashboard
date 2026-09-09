'use client';
import {Slider as Primitive} from '@base-ui/react/slider';
import type {ComponentProps} from 'react';
// The catalog wrapper does not pass an accessible name to its nested range input.
export function Slider({'aria-label':label,...props}:ComponentProps<typeof Primitive.Root>){return <Primitive.Root {...props} data-slot="slider" thumbAlignment="edge"><Primitive.Control><Primitive.Track data-slot="slider-track"><Primitive.Indicator data-slot="slider-range"/></Primitive.Track><Primitive.Thumb data-slot="slider-thumb" index={0} getAriaLabel={()=>label||'Value'}/></Primitive.Control></Primitive.Root>}
