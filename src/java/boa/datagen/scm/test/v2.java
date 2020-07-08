package boa.datagen.scm.test;

class A extends C
{
	private String s;
	public A(String s)
	{
		this.s=s;
		System.out.println("hello");
	}
	public static void main(String []args)
	{
		A a=new A("hi there");
		System.out.print(a.hashCode());
	}
}